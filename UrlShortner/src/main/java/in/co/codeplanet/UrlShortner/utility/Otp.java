package in.co.codeplanet.UrlShortner.utility;

import java.util.Random;

public class Otp {

    public static String generateOtp(int numberOfArgument){
        Random random=new Random();
        StringBuffer stringBuffer=new StringBuffer();
        for(int i=1;i<=numberOfArgument;i++)
            stringBuffer.append(random.nextInt(0,9));
        return stringBuffer.toString();
    }
}
