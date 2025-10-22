package amgw.amgw.controller;

import amgw.amgw.service.NextcloudSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/sync")
public class NextcloudSyncController {
    private final NextcloudSyncService syncService;

    @PostMapping("/nextcloud")
    public ResponseEntity<Void> sync() {
        syncService.syncAllUsersFromDb();
        return ResponseEntity.ok().build();
    }
}

