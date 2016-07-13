package org.ado.psplib.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class Downloader {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    public String download(String siteUrl) throws IOException {
        return download(new URL(siteUrl).openConnection());
    }

    private static String download(URLConnection con) throws IOException {
        final StringBuilder response = new StringBuilder();
        BufferedReader in = null;

        try {
            con.setRequestProperty("User-Agent", USER_AGENT);
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (UnknownHostException uhe) {
            throw new IOException("unknown host or no network", uhe);
        } catch (Exception e) {
            throw new IOException("cannot download page", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }

        return response.toString();
    }
}
