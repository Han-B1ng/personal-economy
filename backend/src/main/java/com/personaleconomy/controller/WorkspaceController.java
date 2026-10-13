package com.personaleconomy.controller;

import com.personaleconomy.common.response.ApiResponse;
import com.personaleconomy.dto.WorkspaceRequests;
import com.personaleconomy.service.WorkspaceService;
import com.personaleconomy.vo.WorkspaceVO;
import com.personaleconomy.vo.WorkspaceMutationVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Profile("(bootstrap | mysql) & !prod")
public class WorkspaceController {
    private final WorkspaceService service;
    public WorkspaceController(WorkspaceService service) { this.service=service; }
    @GetMapping("/workspace") public ApiResponse<WorkspaceVO> snapshot() { return ApiResponse.success(service.snapshot()); }
    @PostMapping("/goals") public ApiResponse<WorkspaceMutationVO> goal(@Valid @RequestBody WorkspaceRequests.Goal body) {return ApiResponse.success(service.saveGoal(null,body));}
    @PutMapping("/goals/{id}") public ApiResponse<WorkspaceMutationVO> goal(@PathVariable String id,@Valid @RequestBody WorkspaceRequests.Goal body){return ApiResponse.success(service.saveGoal(id,body));}
    @PostMapping("/goals/{id}/complete") public ApiResponse<WorkspaceMutationVO> complete(@PathVariable String id){return ApiResponse.success(service.completeGoal(id));}
    @PostMapping("/tasks") public ApiResponse<WorkspaceMutationVO> task(@Valid @RequestBody WorkspaceRequests.Task body){return ApiResponse.success(service.saveTask(null,body));}
    @PutMapping("/tasks/{id}") public ApiResponse<WorkspaceMutationVO> task(@PathVariable String id,@Valid @RequestBody WorkspaceRequests.Task body){return ApiResponse.success(service.saveTask(id,body));}
    @PatchMapping("/tasks/{id}/status") public ApiResponse<WorkspaceMutationVO> taskStatus(@PathVariable String id,@Valid @RequestBody WorkspaceRequests.Status body){return ApiResponse.success(service.setTaskStatus(id,body.completed()));}
    @DeleteMapping("/tasks/{id}") public ApiResponse<WorkspaceMutationVO> deleteTask(@PathVariable String id){return ApiResponse.success(service.delete("tasks",id));}
    @PostMapping("/events") public ApiResponse<WorkspaceMutationVO> event(@Valid @RequestBody WorkspaceRequests.Event body){return ApiResponse.success(service.saveEvent(null,body));}
    @PutMapping("/events/{id}") public ApiResponse<WorkspaceMutationVO> event(@PathVariable String id,@Valid @RequestBody WorkspaceRequests.Event body){return ApiResponse.success(service.saveEvent(id,body));}
    @DeleteMapping("/events/{id}") public ApiResponse<WorkspaceMutationVO> deleteEvent(@PathVariable String id){return ApiResponse.success(service.delete("events",id));}
    @PostMapping("/categories") public ApiResponse<WorkspaceMutationVO> category(@Valid @RequestBody WorkspaceRequests.Category body){return ApiResponse.success(service.saveCategory(body));}
    @DeleteMapping("/categories/{id}") public ApiResponse<WorkspaceMutationVO> deleteCategory(@PathVariable String id){return ApiResponse.success(service.delete("categories",id));}
    @PostMapping("/finance/records") public ApiResponse<WorkspaceMutationVO> finance(@Valid @RequestBody WorkspaceRequests.FinanceRecord body){return ApiResponse.success(service.saveFinanceRecord(body));}
    @PutMapping("/finance/assets") public ApiResponse<WorkspaceMutationVO> assets(@Valid @RequestBody WorkspaceRequests.Assets body){return ApiResponse.success(service.saveAssets(body));}
    @PutMapping("/profile") public ApiResponse<WorkspaceMutationVO> profile(@Valid @RequestBody WorkspaceRequests.Profile body){return ApiResponse.success(service.saveProfile(body));}
}
