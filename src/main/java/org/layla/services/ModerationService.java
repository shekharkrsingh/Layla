package org.layla.services;



import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ModerationService {
    private final Set<String> forbiddenKeywords = new HashSet<>();
    {
        List<String> forbiddenList = Arrays.asList(
                // English offensive words
                "asshole", "bastard", "bitch", "bloody", "bollocks", "butt", "c*nt", "cocksucker", "cunt",
                "damn", "dick", "dildo", "dyke", "fag", "faggot", "fap", "felch", "flick", "fuck", "gagging",
                "gook", "gyp", "hell", "homo", "hooker", "idiot", "incest", "jackass", "jizz", "kike", "klutz",
                "knee grow", "kys", "lame", "lesbo", "maggot", "mick", "milf", "mong", "moron", "muff", "nazi",
                "negro", "nigga", "nigger", "numbnuts", "paki", "piss", "piss off", "porn", "prick", "queer",
                "rape", "retard", "screw you", "shit", "shithead", "slut", "spastic", "spic", "suck my",
                "swallow", "tard", "thot", "twat", "vagina", "whore", "wop", "zoophile", "zoophilia", "anal",
                "bimbo", "blowjob", "boob", "boobs", "breasts", "butt plug", "clit", "cock", "condom", "cum",
                "deepthroat", "dickhead", "ejaculate", "fellatio", "fisting", "fornicate", "handjob", "hardcore",
                "hentai", "intercourse", "kama sutra", "kinky", "lesbian", "masturbate", "orgasm", "orgy",
                "panties", "pornography", "prostitute", "pussy", "sexting", "shemale", "sodomy", "suck",
                "threesome", "tits", "vibrator", "voyeur",


                "allah", "tawhid", "shirk", "iman", "akhirah", "qadr", "qur'an", "quran", "hadith", "sunnah", "shahada",
                "salah", "zakat", "sawm", "hajj", "wudu", "dua", "dhikr", "jumu'ah", "eid", "al-fitr", "alfitr", "eid al-adha",
                "muhammad", "anbiya", "sharia", "halal", "haram", "ummah", "fitrah", "nafs", "jannah", "jahannam",
                "ijtihad", "ijma", "qiyas", "ulama", "mufti", "fatwa", "madrasah", "masjid", "minbar", "mihrab",
                "takbir", "tasbih", "taqwa", "hijrah", "sirah", "risalah", "fard", "sunnah", "mu’akkadah", "muakkadah", "makruh", "bid’ah", "bidah",
                "kaaba", "imam", "caliph", "khutbah", "umrah", "sadaqah", "niyyah", "shura", "tafsir", "tajweed",
                "mu’min", "munafiq", "mushrik", "mu’adhin", "kafir", "bay'ah", "barzakh", "isra", "mi'raj", "miraj", "bismillah", "kafir",
                "alhamdulillah", "subhanallah", "astaghfirullah", "inshaallah", "mashallah", "takwa", "salam", "ghusl", "adhan", "iqamah",
                "jilbab", "hijab", "niqaab", "sunnat", "hadith", "qudsi", "ashura", "ramadan", "laylat", "al-qadr", "arafah", "zamzam", "alqadr",
                "maqam", "ibrahim", "rakah", "tahajjud", "tarawih", "iftar", "suhoor", "rizq", "khushoo", "sabir", "shukr",
                "ahmad", "aibak", "akbar", "ali", "aluddin", "aurangzeb", "bahadur", "balban", "babur", "bin", "durrani", "firuz", "ghazi", "ghazni",
                "ghiyasuddin", "hyder", "ibrahim", "iltutmish", "jahan", "jalaluddin", "jahangir", "khilji", "mahmud", "muhammad", "nasiruddin",
                "nadir", "of", "qasim", "razia", "shah", "sher", "shah", "sikandar", "sultana", "suri", "tipu", "tughlaq", "timur", "zafar", "zain-ul-abidin", "gaza", "palestine",

                // Indian abusive and suspicious words
                "bhosdi", "bhosdike", "chutiya", "chutiye", "lund", "gaand", "gandu", "madarchod", "behnchod",
                "bsdk", "mc", "bc", "randi", "chod", "chudai", "suar", "kutte", "harami", "nalayak",
                "kutta kamina", "sale", "kamina", "jhatu", "jhant", "gand marna", "gand faad", "chinal",
                "lulli", "loda", "bhen ke lode", "maa ke lode", "chodna", "jaat", "chamar", "bhangi", "neech",
                "hijra", "mehnat ki aulad", "launda", "chhakka"

        );

        forbiddenKeywords.addAll(forbiddenList);
    }
    public ModerationService() {
    }

    public boolean shouldDeleteMessage(String text) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        return containsLink(lowerText) || forbiddenKeywords.stream().anyMatch(lowerText::contains) ;
    }

    private boolean containsLink(String text) {
        return text.matches("(?i).*((https?://|www\\.)\\S+|t\\.me/\\S+|@\\w+).*");
    }

    public void addForbiddenKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            forbiddenKeywords.add(keyword.toLowerCase().trim());
        }
    }

    public void removeForbiddenKeyword(String keyword) {
        if (keyword != null) {
            forbiddenKeywords.remove(keyword.toLowerCase());
        }
    }

    public Set<String> getForbiddenKeywords() {
        return new HashSet<>(forbiddenKeywords); // Optional: to safely view them
    }
}
