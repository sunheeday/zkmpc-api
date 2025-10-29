package com.zkrypto.zkmpc_api.common.utility;

import java.math.BigInteger;
import java.security.SecureRandom;

public class U64IdGenerator {
    // 2^64 - 1 을 BigInteger로 표현한 값 (U64의 최댓값)
    private static final BigInteger U64_MAX_VALUE = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    // 암호학적으로 안전한 무작위 값 생성기 사용
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 무작위 U64 (Unsigned 64-bit Integer) 형태의 ID 문자열을 생성합니다.
     * @return 0 이상 2^64 - 1 이하의 10진수 문자열 ID
     */
    public static String generateU64Id() {
        // 1. 무작위 64비트(8바이트) 배열 생성
        byte[] randomBytes = new byte[8];
        SECURE_RANDOM.nextBytes(randomBytes);

        // 2. 바이트 배열을 BigInteger로 변환
        // BigInteger 생성자는 부호를 포함하므로, 부호 비트가 0이 되도록 배열 앞에 0x00 바이트를 추가합니다.
        // 이는 BigInteger가 양수로 인식하도록 강제하여 0부터 2^64 - 1 사이의 값을 얻게 합니다.
        byte[] positiveBytes = new byte[9];
        // positiveBytes[0]는 0으로 유지되어 양수임을 나타냅니다.
        System.arraycopy(randomBytes, 0, positiveBytes, 1, 8);

        BigInteger randomU64 = new BigInteger(positiveBytes);

        // 3. 생성된 BigInteger가 U64 최대 범위를 벗어나지 않도록 (이중 체크)
        if (randomU64.compareTo(U64_MAX_VALUE) > 0) {
            // 이 코드는 이론상 도달하지 않아야 하지만, 안전성을 위해 처리
            return randomU64.mod(U64_MAX_VALUE.add(BigInteger.ONE)).toString();
        }

        // 4. 10진수 문자열로 반환
        return randomU64.toString();
    }

}
