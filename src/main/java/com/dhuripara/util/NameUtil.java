package com.dhuripara.util;

import com.dhuripara.model.Member;

public class NameUtil {

    private NameUtil() {}

    public static String buildMemberName(Member member) {
        if (member == null) return "";
        String bnFirst = member.getFirstNameBn();
        String bnLast = member.getLastNameBn();
        if (bnFirst != null && !bnFirst.isBlank()) {
            String full = (bnFirst + " " + (bnLast != null ? bnLast : "")).trim();
            if (!full.isEmpty()) return full;
        }
        String first = member.getFirstName();
        String last = member.getLastName();
        String full = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
        if (!full.isEmpty()) return full;
        return "";
    }
}
