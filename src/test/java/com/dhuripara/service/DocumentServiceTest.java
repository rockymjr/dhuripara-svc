package com.dhuripara.service;

import com.dhuripara.dto.request.DocumentUploadRequest;
import com.dhuripara.model.DocumentCategory;
import com.dhuripara.model.Member;
import com.dhuripara.model.MemberDocument;
import com.dhuripara.repository.DocumentCategoryRepository;
import com.dhuripara.repository.MemberDocumentRepository;
import com.dhuripara.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private MemberDocumentRepository documentRepository;

    @Mock
    private DocumentCategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OracleObjectStorageService objectStorageService;

    @InjectMocks
    private DocumentService documentService;

    @Test
    public void uploadDocument_setsUploadedBy_and_saves() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        Member member = new Member();
        member.setId(memberId);

        DocumentCategory category = new DocumentCategory();
        category.setId(categoryId);
        category.setIsActive(true);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{1,2,3});
        when(objectStorageService.uploadFile(eq(file), anyString())).thenReturn("members/" + memberId + "/test.pdf");

        DocumentUploadRequest req = new DocumentUploadRequest();
        req.setMemberId(memberId);
        req.setDocumentCategoryId(categoryId);
        req.setFile(file);

        when(documentRepository.save(any(MemberDocument.class))).thenAnswer(i -> i.getArgument(0));

        documentService.uploadDocument(req, uploaderId);

        ArgumentCaptor<MemberDocument> captor = ArgumentCaptor.forClass(MemberDocument.class);
        verify(documentRepository, times(1)).save(captor.capture());

        MemberDocument saved = captor.getValue();
        assertThat(saved.getUploadedBy()).isEqualTo(uploaderId);
        assertThat(saved.getObjectStoragePath()).contains("members/" + memberId + "/test.pdf");
    }
}
