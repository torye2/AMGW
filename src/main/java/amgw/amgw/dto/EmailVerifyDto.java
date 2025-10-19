package amgw.amgw.dto;

public class EmailVerifyDto {
    // 요청 DTO (재전송 시작은 보통 인증 사용자 기준이라 payload가 없어도 됨)
    public record EmailVerifyStartRequest(
            Long userId  // (옵션) 관리자가 특정 사용자에게 재전송하는 경우 등
    ) {}

    // 응답 DTO
    public record EmailVerifyStartResponse(
            boolean ok, String message
    ) {}

    public record EmailVerifyConfirmResponse(
            boolean ok, String message
    ) {}

}
