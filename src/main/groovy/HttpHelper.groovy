package navimateforbusiness

/**
 * Created by Siddharth on 07-09-2017.
 */
class HttpHelper {
    // ----------------------- Constants ----------------------- //
    private static final String TAG = "HTTP_HELPER";

    // ----------------------- Classes ---------------------------//
    // ----------------------- Interfaces ----------------------- //
    // ----------------------- Globals ----------------------- //
    private String urlAddress = "";

    // ----------------------- Constructor ----------------------- //
    HttpHelper(String url)
    {
        urlAddress = url;
    }

    // ----------------------- Overrides ----------------------- //
    // ----------------------- Public APIs ----------------------- //
    String Execute(String requestString, String requestMethod, HashMap<String, String> requestProperties)
    {
        String response             = "";
        BufferedReader ipReader     = null;
        BufferedWriter opWriter     = null;
        URL urlObj                  = null;
        HttpURLConnection urlConn   = null;

        if (requestProperties == null)
        {
            requestProperties = new HashMap<String, String>();
        }

        try
        {
            // Connect to URL
            urlObj = new URL(urlAddress);
            urlConn = (HttpURLConnection) urlObj.openConnection();

            // Set Connection parameters
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(requestMethod);

            for (String key : requestProperties.keySet())
            {
                urlConn.setRequestProperty(key, requestProperties.get(key));
            }

            // Write to stream if request string is valid
            if ((requestString != null) && (requestString.length() > 0))
            {
                // Initialize output stream on the URL Connection
                opWriter = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8"));

                // Send data as String on the output stream of connection
                opWriter.write(requestString);
                opWriter.flush();
                opWriter.close();
            }


            // Initialize input stream on the URL Connection
            ipReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            // Get and parse response
            StringBuilder sb = new StringBuilder();
            String line = "";

            while ((line = ipReader.readLine()) != null)
            {
                sb.append(line);
            }
            ipReader.close();

            // Parse repsonse
            response = sb.toString();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            urlConn.disconnect();
        }

        return response;
    }

    // ----------------------- Private APIs ----------------------- //
}
