# JWT Secret Key Configuration Guide

## Current Implementation

The application uses HS256 (HMAC with SHA-256) algorithm for JWT token signing. The secret key can be:
1. **Base64 encoded** (recommended)
2. **Plain text** (must be at least 32 characters/256 bits)

## Generating a Secure JWT Secret

### Option 1: Generate Base64 Secret (Recommended)

#### Using OpenSSL (Linux/Mac):
```bash
openssl rand -base64 32
```

#### Using Node.js:
```javascript
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

#### Using Python:
```python
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### Using Java:
```java
import java.security.SecureRandom;
import java.util.Base64;

byte[] key = new byte[32];
new SecureRandom().nextBytes(key);
String secret = Base64.getEncoder().encodeToString(key);
System.out.println(secret);
```

### Option 2: Use Plain Text Secret

If not using Base64, ensure your secret is **at least 32 characters** long:
```
myVerySecureJWTSecretKey12345678901234567890
```

## Configuration

### Environment Variable (Production)
```bash
export JWT_SECRET="your-base64-encoded-secret-here"
```

### Docker Compose
```yaml
environment:
  JWT_SECRET: "your-base64-encoded-secret-here"
```

### application.yml (Development Only)
```yaml
jwt:
  secret: ${JWT_SECRET:dGhpc0lzQVNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb24=}
```

## Security Best Practices

1. **Never commit secrets to version control**
2. **Use different secrets for different environments** (dev, staging, production)
3. **Rotate secrets periodically**
4. **Use strong, random secrets** (minimum 256 bits)
5. **Store secrets in secure vaults** (AWS Secrets Manager, HashiCorp Vault, etc.)

## Validating JWT Tokens Online

When testing with online JWT validators (like jwt.io):
- Use the **Base64 encoded secret** directly
- Select **HS256** algorithm
- The signature should validate successfully

## Current Default Secret

The application ships with a default Base64-encoded secret for development:
```
dGhpc0lzQVNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb24=
```

**⚠️ WARNING: Change this immediately for production use!**

## Testing JWT with the Default Secret

If you want to validate a JWT token on jwt.io with the default secret:

1. Copy your JWT token
2. Go to https://jwt.io/
3. Paste the token
4. In the "Verify Signature" section:
   - Keep algorithm as **HS256**
   - Paste the Base64 secret: `dGhpc0lzQVNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb24=`
   - Signature should show as verified ✅

## Troubleshooting

### "Invalid Signature" Error
- Ensure you're using the correct secret
- Verify the secret is Base64 encoded if that's what your config expects
- Check that the secret length is at least 32 bytes (256 bits)

### "User Not Found" After Registration
- This has been fixed in `JwtAuthenticationFilter.java`
- Only `/auth/register` and `/auth/login` are public
- `/auth/me` requires JWT authentication
