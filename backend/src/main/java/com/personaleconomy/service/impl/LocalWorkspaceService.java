package com.personaleconomy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personaleconomy.common.exception.WorkspaceException;
import com.personaleconomy.vo.WorkspaceMutationVO;
import com.personaleconomy.vo.WorkspaceVO;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Single-user local review storage. Deliberately disabled in production/MySQL profiles. */
@Service
@Profile("bootstrap & !mysql & !prod")
public class LocalWorkspaceService extends AbstractWorkspaceService {
    private final Path file;
    private ObjectNode state;
    public LocalWorkspaceService(ObjectMapper mapper,@Value("${personal-economy.data-file}") String path) throws IOException {
        super(mapper); file=Path.of(path).toAbsolutePath().normalize();
        if(Files.exists(file)) state=(ObjectNode)mapper.readTree(file.toFile());
        else try(var input=new ClassPathResource("fixtures/workspace-demo.json").getInputStream()) {
            state=(ObjectNode)mapper.readTree(input); persist(state);
        }
        if(!"local-demo".equals(state.path("source").asText())) throw new IOException("Unsupported local workspace format");
        mapper.treeToValue(state,WorkspaceVO.class);
    }
    public synchronized WorkspaceVO snapshot(){return mapper.convertValue(state,WorkspaceVO.class);}
    protected synchronized WorkspaceMutationVO change(String id,Consumer<ObjectNode> action){
        ObjectNode next=state.deepCopy();action.accept(next);next.put("updatedAt",Instant.now().toString());
        try {persist(next);}catch(IOException e){throw new WorkspaceException(HttpStatus.INTERNAL_SERVER_ERROR,"本地保存失败，请检查数据目录后重试");}
        state=next;return new WorkspaceMutationVO(snapshot(),id);
    }
    private void persist(ObjectNode next) throws IOException {
        Files.createDirectories(file.getParent());
        Path tmp=Files.createTempFile(file.getParent(),"workspace-",".tmp");
        try {mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(),next);
            try{Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }
}
