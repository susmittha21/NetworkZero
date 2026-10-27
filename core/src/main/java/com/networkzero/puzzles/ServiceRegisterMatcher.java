package com.networkzero.puzzles;

import java.util.Map;

/** Pure Room 2 service/port matching rules. */
public final class ServiceRegisterMatcher {
    public static final Map<String, String> ANSWER = Map.of(
        "SSH", "22", "HTTPS", "443", "DNS", "53", "FTP", "21", "SMTP", "25", "RDP", "3389");
    private ServiceRegisterMatcher() {}
    public static boolean correct(String service, String port) { return ANSWER.get(service) != null && ANSWER.get(service).equals(port); }
    public static boolean correct(String service,String port,Map<String,String> answer){return answer.get(service)!=null&&answer.get(service).equals(port);}
    public static int countCorrect(Map<String, String> pairs) {
        return countCorrect(pairs,ANSWER);
    }
    public static int countCorrect(Map<String,String> pairs,Map<String,String> answer) {
        int count = 0;
        for (Map.Entry<String, String> pair : pairs.entrySet()) if (correct(pair.getKey(), pair.getValue(),answer)) count++;
        return count;
    }
    public static boolean complete(Map<String, String> pairs) { return countCorrect(pairs) == ANSWER.size() && pairs.size() == ANSWER.size(); }
    public static boolean complete(Map<String,String> pairs,Map<String,String> answer){return countCorrect(pairs,answer)==answer.size()&&pairs.size()==answer.size();}
}
