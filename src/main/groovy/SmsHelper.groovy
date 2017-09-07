package navimateforbusiness

public class SmsHelper
{
    // ----------------------- Constants ----------------------- //
    private static final String TAG = "SMS_HELPER";

    // SMS Related macros
    private static final String SENDERID                = "NVMATE";
    private static final String ROUTE_INTERNATIONAL     = "INTERNATIONAL";
    private static final String ROUTE_LOCAL             = "TEMPLATE_BASED";
    private static final String API_KEY_24_7_SMS        = "9bMhvPfXZkQ";

    // ----------------------- APIs ----------------------- //
    static boolean SendSms(String number, String message)
    {
        // Get SMS URL
        String smsUrl = GetSmsUrl(number, message);
        if (smsUrl.length() == 0)
        {
            return false;
        }

        // Send Request
        navimateforbusiness.HttpHelper httpHelper = new navimateforbusiness.HttpHelper(smsUrl);
        String messageId = httpHelper.Execute("", "GET", new HashMap<String, String>());

        // Check Returned MSG ID
        return IsMsgIdValid(messageId);
    }

    // ----------------------- Private APIs ----------------------- //
    private static String GetSmsUrl(String number, String message)
    {
        String urlString = "";

        String serviceName = ROUTE_INTERNATIONAL;
        if (number.contains("+91"))
        {
            serviceName = ROUTE_LOCAL;
        }

        // Remove '+' from number
        if (number.contains("+"))
        {
            StringBuilder tempString = new StringBuilder(number);
            tempString.deleteCharAt(0);
            number = tempString.toString();
        }

        try
        {
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            urlString   =    "http://smsapi.24x7sms.com/api_2.0/SendSMS.aspx?" +
                    "APIKEY="           + API_KEY_24_7_SMS  +
                    "&MobileNo="        + number            +
                    "&SenderID="        + SENDERID          +
                    "&Message="         + encodedMessage    +
                    "&ServiceName="     + serviceName;
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return urlString;
    }

    private static boolean IsMsgIdValid(String msgId)
    {
        System.out.print("MSG = " + msgId)
        if (msgId.contains("MsgID") && msgId.contains(":"))
        {
            String[] messageParts = msgId.split(":");
            if (messageParts.length >= 4)
            {
                return true;
            }
        }

        return false;
    }

}
