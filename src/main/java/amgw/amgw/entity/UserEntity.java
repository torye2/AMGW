package amgw.amgw.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "users") // ← DataSource가 gw를 가리키므로 catalog 지정 불필요
@Getter @Setter
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private String subject;
    private String username;
    private String email;
    private String name;

    @Column(columnDefinition = "text") // JSON 타입이면 columnDefinition="json"도 가능(DDL과 일치)
    private String rolesJson;

    private String picture;
    private LocalDateTime lastLoginAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }
}