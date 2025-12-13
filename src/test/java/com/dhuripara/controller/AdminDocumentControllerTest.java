package com.dhuripara.controller;

import com.dhuripara.dto.request.DocumentUploadRequest;
import com.dhuripara.dto.response.MemberDocumentResponse;
import com.dhuripara.repository.AdminUserRepository;
import com.dhuripara.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminDocumentController controller;

    @Test
    public void upload_createsAdminUserIfMissing_and_callsService() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("MEMBER_" + uploaderId.toString());

        when(adminUserRepository.existsById(uploaderId)).thenReturn(false);
        when(documentService.uploadDocument(any(DocumentUploadRequest.class), eq(uploaderId)))
                .thenReturn(new MemberDocumentResponse());

        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});

        controller.uploadDocument(memberId, categoryId, file, "notes", auth);

        verify(adminUserRepository, times(1)).save(any());
        verify(documentService, times(1)).uploadDocument(any(DocumentUploadRequest.class), eq(uploaderId));
    }
}
