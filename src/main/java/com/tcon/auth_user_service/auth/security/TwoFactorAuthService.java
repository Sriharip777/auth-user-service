package com.tcon.auth_user_service.auth.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class TwoFactorAuthService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    public String generateSecretKey() {
        return secretGenerator.generate();
    }

    /**
     * Generate QR code as Base64 encoded image
     */
    public String getQrCodeUrl(String email, String secret) {
        try {
            String otpAuthUri = String.format(
                    "otpauth://totp/TutoringPlatform:%s?secret=%s&issuer=TutoringPlatform",
                    URLEncoder.encode(email, StandardCharsets.UTF_8),
                    secret
            );

            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(otpAuthUri, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            String base64Image = Base64.getEncoder().encodeToString(pngData);
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return null;
        }
    }

    public boolean validateCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.error("Error validating 2FA code", e);
            return false;
        }
    }
}
