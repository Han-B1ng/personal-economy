package com.personaleconomy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personaleconomy.common.exception.WorkspaceException;
import com.personaleconomy.dto.WorkspaceRequests;
import com.personaleconomy.service.WorkspaceService;
import com.personaleconomy.vo.WorkspaceMutationVO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.http.HttpStatus;

/** Shared business rules for file and relational storage adapters. */
public abstract class AbstractWorkspaceService implements WorkspaceService {
    protected final ObjectMapper mapper;
    protected AbstractWorkspaceService(ObjectMapper mapper) { this.mapper = mapper; }
    protected abstract WorkspaceMutationVO change(String id, Consumer<ObjectNode> action);
    protected ObjectNode entry(ObjectNode root,String collection,String id){
        for(var n:root.withArray(collection)) if(id.equals(n.path("id").asText()))return (ObjectNode)n;
        throw new WorkspaceException(HttpStatus.NOT_FOUND,"记录不存在或已被删除");
    }
    private String optionalId(String id){return id==null||id.isBlank()?null:id;}
    private void category(ObjectNode root,String id){if(!entry(root,"categories",id).path("active").asBoolean())throw new WorkspaceException(HttpStatus.BAD_REQUEST,"分类已停用，请选择其他分类");}
    private String id(String current){return current==null?UUID.randomUUID().toString():current;}
    public WorkspaceMutationVO saveGoal(String current,WorkspaceRequests.Goal body){String id=id(current);return change(id,root->{
        if(body.dueDate().isBefore(body.startDate()))throw new WorkspaceException(HttpStatus.BAD_REQUEST,"截止日期不能早于开始日期");
        category(root,body.categoryId()); ObjectNode n=mapper.valueToTree(body);
        n.put("id",id);n.put("status",current==null?"ACTIVE":entry(root,"goals",id).path("status").asText());
        if(current!=null)n.set("completedDate",entry(root,"goals",id).get("completedDate"));
        replace(root,"goals",current,n);
    });}
    public WorkspaceMutationVO completeGoal(String id){return change(id,root->{ObjectNode n=entry(root,"goals",id);if(!"COMPLETED".equals(n.path("status").asText())){n.put("status","COMPLETED");n.put("completedDate",LocalDate.now().toString());}});}
    public WorkspaceMutationVO saveTask(String current,WorkspaceRequests.Task body){String id=id(current);return change(id,root->{
        String goal=optionalId(body.goalId());if(goal!=null)entry(root,"goals",goal);
        ObjectNode n=mapper.valueToTree(body);n.put("id",id);n.put("goalId",goal);n.put("completed",current!=null&&entry(root,"tasks",id).path("completed").asBoolean());replace(root,"tasks",current,n);
    });}
    public WorkspaceMutationVO setTaskStatus(String id,boolean completed){return change(id,root->entry(root,"tasks",id).put("completed",completed));}
    public WorkspaceMutationVO saveEvent(String current,WorkspaceRequests.Event body){String id=id(current);return change(id,root->{
        category(root,body.categoryId());String goal=optionalId(body.sourceGoalId());if(goal!=null)entry(root,"goals",goal);
        ObjectNode n=mapper.valueToTree(body);n.put("id",id);n.put("sourceGoalId",goal);n.put("evaluationStatus","PENDING");n.putNull("contribution");replace(root,"events",current,n);
    });}
    public WorkspaceMutationVO saveCategory(WorkspaceRequests.Category body){String id=id(null);return change(id,root->{
        for(var n:root.withArray("categories"))if(n.path("name").asText().equals(body.name().trim()))throw new WorkspaceException(HttpStatus.BAD_REQUEST,"分类名称已存在");
        ObjectNode n=mapper.createObjectNode().put("id",id).put("name",body.name().trim()).put("system",false).put("active",true);root.withArray("categories").add(n);
    });}
    public WorkspaceMutationVO delete(String collection,String id){return change(id,root->{
        ObjectNode n=entry(root,collection,id);
        if("categories".equals(collection)){
            if(n.path("system").asBoolean())throw new WorkspaceException(HttpStatus.BAD_REQUEST,"系统分类不可删除");
            n.put("active",false);return;
        }
        ArrayNode a=root.withArray(collection);for(int i=0;i<a.size();i++)if(id.equals(a.get(i).path("id").asText())){a.remove(i);return;}
    });}
    public WorkspaceMutationVO saveFinanceRecord(WorkspaceRequests.FinanceRecord body){String id=id(null);return change(id,root->{ObjectNode n=mapper.valueToTree(body);n.put("id",id);root.withArray("financeRecords").add(n);});}
    public WorkspaceMutationVO saveAssets(WorkspaceRequests.Assets body){return change(null,root->{ObjectNode n=mapper.valueToTree(body);n.put("updatedAt",Instant.now().toString());root.set("assets",n);});}
    public WorkspaceMutationVO saveProfile(WorkspaceRequests.Profile body){return change(null,root->{
        if(body.avatar()!=null&&!body.avatar().isEmpty()&&!body.avatar().matches("data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/=\\r\\n]+"))throw new WorkspaceException(HttpStatus.BAD_REQUEST,"头像仅支持PNG、JPEG或WebP图片");
        ObjectNode n=mapper.valueToTree(body);n.put("stockCode",root.path("profile").path("stockCode").asText());root.set("profile",n);
    });}
    private void replace(ObjectNode root,String collection,String current,ObjectNode next){ArrayNode a=root.withArray(collection);if(current==null){a.add(next);return;}entry(root,collection,current);for(int i=0;i<a.size();i++)if(current.equals(a.get(i).path("id").asText())){a.set(i,next);return;}}
}
