package com.ecnu.service.impl;

import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.mapper.AssessmentForm1Mapper;
import com.ecnu.mapper.AssessmentFeedbackMapper;
import com.ecnu.mapper.AssessmentTaskMapper;
import com.ecnu.model.dto.AssessmentForm1SubmitDTO;
import com.ecnu.model.entity.AssessmentForm1;
import com.ecnu.model.entity.AssessmentTask;
import com.ecnu.service.ChildDoctorBinding;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentTaskRepetitiveBehaviorTest {
    @Mock private AssessmentTaskMapper taskMapper;
    @Mock private AssessmentForm1Mapper formMapper;
    @Mock private AssessmentFeedbackMapper feedbackMapper;
    @Mock private ChildDoctorBinding childDoctorBinding;
    @InjectMocks private AssessmentTaskServiceImpl service;

    private AssessmentForm1SubmitDTO form(String behavior) {
        return AssessmentForm1SubmitDTO.builder().taskId(42L).diseaseType("TD")
                .confidence(5).diagnosticBasis("视频观察依据")
                .socialResponse(1).eyeContact(1).socialReciprocity(1).socialEmotionalExpression(1)
                .repetitiveBehavior(behavior).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"无", "有，严重程度：1分", "有，严重程度：2分", "有，严重程度：3分",
            "有，严重程度：4分", "有，严重程度：5分", "  无  "})
    void savesSelectionInExistingTextField(String behavior) {
        when(taskMapper.getByIdForUpdate(42L)).thenReturn(AssessmentTask.builder()
                .id(42L).doctorId(9L).taskStatus(1).currentStep(1).build());
        when(formMapper.update(any())).thenReturn(true);
        when(taskMapper.updateCurrentStep(42L, 2)).thenReturn(true);

        assertTrue(service.submitForm1(form(behavior)));

        var captured = ArgumentCaptor.forClass(AssessmentForm1.class);
        verify(formMapper).update(captured.capture());
        assertEquals(behavior.trim(), captured.getValue().getRepetitiveBehavior());
        verify(childDoctorBinding).checkAssessmentAccess(9L);
        verify(taskMapper).updateCurrentStep(42L, 2);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "有", "有，严重程度：0分", "有，严重程度：6分", "有，严重程度：2.5分",
            "有，严重程度：-1分", "有，严重程度：分", "无，严重程度：3分", "反复摆手",
            "有，严重程度：3分附加文字", "{\"present\":true}"})
    void rejectsMissingOrInvalidScoreBeforeWriting(String behavior) {
        var error = assertThrows(BusinessException.class, () -> service.submitForm1(form(behavior)));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), error.getCode());
        verifyNoInteractions(formMapper, taskMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"反复摆手，偶有转圈", "未观察到", "无", "有，严重程度：4分"})
    void readsExistingTextWithoutRewritingHistory(String behavior) {
        when(formMapper.getByTaskId(42L)).thenReturn(AssessmentForm1.builder()
                .taskId(42L).repetitiveBehavior(behavior).build());
        when(taskMapper.getById(42L)).thenReturn(AssessmentTask.builder().id(42L).doctorId(9L).build());
        assertEquals(behavior, service.getForm1ByTaskId(42L).getRepetitiveBehavior());
        verify(formMapper, never()).update(any());
    }
}
