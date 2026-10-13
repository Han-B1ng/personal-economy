package com.personaleconomy.service;
import com.personaleconomy.dto.WorkspaceRequests;
import com.personaleconomy.vo.WorkspaceVO;
import com.personaleconomy.vo.WorkspaceMutationVO;
public interface WorkspaceService {
    WorkspaceVO snapshot();
    WorkspaceMutationVO saveGoal(String id, WorkspaceRequests.Goal request);
    WorkspaceMutationVO completeGoal(String id);
    WorkspaceMutationVO saveTask(String id, WorkspaceRequests.Task request);
    WorkspaceMutationVO setTaskStatus(String id, boolean completed);
    WorkspaceMutationVO saveEvent(String id, WorkspaceRequests.Event request);
    WorkspaceMutationVO saveCategory(WorkspaceRequests.Category request);
    WorkspaceMutationVO delete(String collection, String id);
    WorkspaceMutationVO saveFinanceRecord(WorkspaceRequests.FinanceRecord request);
    WorkspaceMutationVO saveAssets(WorkspaceRequests.Assets request);
    WorkspaceMutationVO saveProfile(WorkspaceRequests.Profile request);
}
