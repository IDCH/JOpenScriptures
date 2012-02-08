/**
 * 
 */
package org.idch.bible.ref;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Adapted from PHP code by Weston Reuter,
 * @see {@link http://code.google.com/p/open-scriptures/source/browse/branches/php-prototypes/reference-parser.lib.php }
 * @author Neal Audenaert
 * @author Weston Reuter
 */
public class ReferenceParser {

    private static Map<String, Pattern> patterns = null;
    
    static {
        patterns = new HashMap<String, Pattern>();
        
        defineAbbreviations(1, "Gen", "Genesis",            "Gen", "Ge", "Gn");
        defineAbbreviations(2, "Exod", "Exodus",            "Exo", "Ex", "Exod");
        defineAbbreviations(3, "Lev", "Leviticus",          "Lev", "Le", "Lv");
        defineAbbreviations(4, "Num", "Numbers",            "Num", "Nu", "Nm", "Nb");
        defineAbbreviations(5, "Deut", "Deuteronomy",       "Deut", "Dt");
        defineAbbreviations(6, "Josh", "Joshua",            "Josh", "Jos", "Jsh");
        defineAbbreviations(7, "Judg", "Judges",            "Judg", "Jdg", "Jg", "Jdgs");
        defineAbbreviations(8, "Ruth", "Ruth",              "Rth", "Ru");
        defineAbbreviations(9, "1Sam", "1 Samuel",          "1 Sam", "1 Sa", "1Samuel", "1S", "I Sa", "1 Sm", "1Sa", "I Sam", "1Sam", "I Samuel", "1st Samuel", "First Samuel");
        defineAbbreviations(10, "2Sam", "2 Samuel",         "2 Sam", "2 Sa", "2S", "II Sa", "2 Sm", "2Sa", "II Sam", "2Sam", "II Samuel", "2Samuel", "2nd Samuel", "Second Samuel");
        defineAbbreviations(11, "1Kgs", "1 Kings",          "1 Kgs", "1 Ki", "1K", "I Kgs", "1Kgs", "I Ki", "1Ki", "I Kings", "1Kings", "1st Kgs", "1st Kings", "First Kings", "First Kgs", "1Kin");
        defineAbbreviations(12, "2Kgs", "2 Kings",          "2 Kgs", "2 Ki", "2K", "II Kgs", "2Kgs", "II Ki", "2Ki", "II Kings", "2Kings", "2nd Kgs", "2nd Kings", "Second Kings", "Second Kgs", "2Kin");
        defineAbbreviations(13, "1Chr", "1 Chronicles",     "1 Chron", "1 Ch", "I Ch", "1Ch", "1 Chr", "I Chr", "1Chr", "I Chron", "1Chron", "I Chronicles", "1Chronicles", "1st Chronicles", "First Chronicles");
        defineAbbreviations(14, "1Chr", "2 Chronicles",     "2 Chron", "2 Ch", "II Ch", "2Ch", "II Chr", "2Chr", "II Chron", "2Chron", "II Chronicles", "2Chronicles", "2nd Chronicles", "Second Chronicles");
        defineAbbreviations(15, "Ezra", "Ezra",             "Ezra", "Ezr");
        defineAbbreviations(16, "Neh", "Nehemiah",          "Neh", "Ne");
        defineAbbreviations(17, "Esth", "Esther",           "Esth", "Es");
        defineAbbreviations(18, "Job", "Job",               "Job", "Job", "Jb");
        defineAbbreviations(19, "Ps", "Psalm",              "Pslm", "Ps", "Psalms", "Psa", "Psm", "Pss");
        defineAbbreviations(20, "Prov", "Proverbs",         "Prov", "Pr", "Prv");
        defineAbbreviations(21, "Eccl", "Ecclesiastes",     "Eccles", "Ec", "Qoh", "Qoheleth");
        defineAbbreviations(22, "Song", "Song of Solomon",  "Song", "So", "Canticle of Canticles", "Canticles", "Song of Songs", "SOS");
        defineAbbreviations(23, "Isa", "Isaiah",            "Isa", "Is");
        defineAbbreviations(24, "Jer", "Jeremiah",          "Jer", "Je", "Jr");
        defineAbbreviations(25, "Lam", "Lamentations",      "Lam", "La");
        defineAbbreviations(26, "Ezek", "Ezekiel",          "Ezek", "Eze", "Ezk");
        defineAbbreviations(27, "Dan", "Daniel",            "Dan", "Da", "Dn");
        defineAbbreviations(28, "Hos", "Hosea",             "Hos", "Ho");
        defineAbbreviations(29, "Joel", "Joel",             "Joel", "Joe", "Jl");
        defineAbbreviations(30, "Amos", "Amos",             "Amos", "Am");
        defineAbbreviations(31, "Obad", "Obadiah",          "Obad", "Ob");
        defineAbbreviations(32, "Jonah", "Jonah",           "Jnh", "Jon");
        defineAbbreviations(33, "Mic", "Micah",             "Micah", "Mic");
        defineAbbreviations(34, "Nah", "Nahum",             "Nah", "Na");
        defineAbbreviations(35, "Hab", "Habakkuk",          "Hab", "Hab");
        defineAbbreviations(36, "Zeph", "Zephaniah",        "Zeph", "Zep", "Zp");
        defineAbbreviations(37, "Hag", "Haggai",            "Haggai", "Hag", "Hg");
        defineAbbreviations(38, "Zech", "Zechariah",        "Zech", "Zec", "Zc");
        defineAbbreviations(39, "Mal", "Malachi",           "Mal", "Mal", "Ml");
        defineAbbreviations(40, "Matt", "Matthew",          "Matt", "Mt");
        defineAbbreviations(41, "Mark", "Mark",             "Mrk", "Mk", "Mr");
        defineAbbreviations(42, "Luke", "Luke",             "Luk", "Lk");
        defineAbbreviations(43, "John", "John",             "John", "Jn", "Jhn");
        defineAbbreviations(44, "Acts", "Acts",             "Acts", "Ac");
        defineAbbreviations(45, "Rom", "Romans",            "Rom", "Ro", "Rm");
        defineAbbreviations(46, "1Cor", "1 Corinthians",    "1 Cor", "1 Co", "I Co", "1Co", "I Cor", "1Cor", "I Corinthians", "1Corinthians", "1st Corinthians", "First Corinthians");
        defineAbbreviations(47, "2Cor", "2 Corinthians",    "2 Cor", "2 Co", "II Co", "2Co", "II Cor", "2Cor", "II Corinthians", "2Corinthians", "2nd Corinthians", "Second Corinthians");
        defineAbbreviations(48, "Galatians",                "Gal", "Ga");
        defineAbbreviations(49, "Eph", "Ephesians",         "Ephes", "Eph");
        defineAbbreviations(50, "Phil", "Philippians",      "Phil", "Php");
        defineAbbreviations(51, "Col", "Colossians",        "Col", "Col");
        defineAbbreviations(52, "1Thess", "1 Thessalonians","1 Thess", "1 Th", "I Th", "1Th", "I Thes", "1Thes", "I Thess", "1Thess", "I Thessalonians", "1Thessalonians", "1st Thessalonians", "First Thessalonians");
        defineAbbreviations(53, "2Thess", "2 Thessalonians","2 Thess", "2 Th", "II Th", "2Th", "II Thes", "2Thes", "II Thess", "2Thess", "II Thessalonians", "2Thessalonians", "2nd Thessalonians", "Second Thessalonians");
        defineAbbreviations(54, "1Tim", "1 Timothy",        "1 Tim", "1 Ti", "I Ti", "1Ti", "I Tim", "1Tim", "I Timothy", "1Timothy", "1st Timothy", "First Timothy");
        defineAbbreviations(55, "1Tim", "2 Timothy",        "2 Tim", "2 Ti", "II Ti", "2Ti", "II Tim", "2Tim", "II Timothy", "2Timothy", "2nd Timothy", "Second Timothy");
        defineAbbreviations(56, "Titus", "Titus",           "Titus", "Tit");
        defineAbbreviations(57, "Phlm", "Philemon",         "Philem", "Phm");
        defineAbbreviations(58, "Heb", "Hebrews",           "Hebrews", "Heb");
        defineAbbreviations(59, "Jas", "James",             "James", "Jas", "Jm");
        defineAbbreviations(60, "1Pet", "1 Peter",          "1 Pet", "1 Pe", "I Pe", "1Pe", "I Pet", "1Pet", "I Pt", "1 Pt", "1Pt", "I Peter", "1Peter", "1st Peter", "First Peter");
        defineAbbreviations(61, "2Pet", "2 Peter",          "2 Pet", "2 Pe", "II Pe", "2Pe", "II Pet", "2Pet", "II Pt", "2 Pt", "2Pt", "II Peter", "2Peter", "2nd Peter", "Second Peter");
        defineAbbreviations(62, "1John", "1 John",          "1 John", "1 Jn", "I Jn", "1Jn", "I Jo", "1Jo", "I Joh", "1Joh", "I Jhn", "1 Jhn", "1Jhn", "I John", "1John", "1st John", "First John");
        defineAbbreviations(63, "2John", "2 John",          "2 John", "2 Jn", "II Jn", "2Jn", "II Jo", "2Jo", "II Joh", "2Joh", "II Jhn", "2 Jhn", "2Jhn", "II John", "2John", "2nd John", "Second John");
        defineAbbreviations(64, "3John", "3 John",          "3 John", "3 Jn", "III Jn", "3Jn", "III Jo", "3Jo", "III Joh", "3Joh", "III Jhn", "3 Jhn", "3Jhn", "III John", "3John", "3rd John", "Third John");
        defineAbbreviations(65, "Jude","Jude",              "Jude", "Jud");
        defineAbbreviations(66, "Rev", "Revelation",        "Rev", "Re", "The Revelation");
        
//      patterns.put("1Chr",    Pattern.compile("1 Chronicles",             Pattern.CASE_INSENSITIVE));  
//      patterns.put("1Kgs",    Pattern.compile("1 Kings",                  Pattern.CASE_INSENSITIVE)); 
//      patterns.put("1Sam",    Pattern.compile("1 Samuel",                 Pattern.CASE_INSENSITIVE));  
//      patterns.put("2Chr",    Pattern.compile("2 Chronicles",             Pattern.CASE_INSENSITIVE));  
//      patterns.put("2Kgs",    Pattern.compile("2 Kings",                  Pattern.CASE_INSENSITIVE)); 
//      patterns.put("2Sam",    Pattern.compile("2 Samuel",                 Pattern.CASE_INSENSITIVE));
//      
//      patterns.put("Amos",    Pattern.compile("Amos",                     Pattern.CASE_INSENSITIVE));  
//      patterns.put("Dan",     Pattern.compile("Daniel",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Deut",    Pattern.compile("Deuteronomy",              Pattern.CASE_INSENSITIVE));  
//      patterns.put("Eccl",    Pattern.compile("Ecclesiastes",             Pattern.CASE_INSENSITIVE));  
//      patterns.put("Esth",    Pattern.compile("Esther",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Exod",    Pattern.compile("Exodus",                   Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Ezek",    Pattern.compile("Ezekiel",                  Pattern.CASE_INSENSITIVE));  
//      patterns.put("Ezra",    Pattern.compile("Ezra",                     Pattern.CASE_INSENSITIVE));  
//      patterns.put("Gen",     Pattern.compile("Gensis",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Hab",     Pattern.compile("Habakkuk",                 Pattern.CASE_INSENSITIVE));  
//      patterns.put("Hag",     Pattern.compile("Haggai",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Hos",     Pattern.compile("Hosea",                    Pattern.CASE_INSENSITIVE));  
//      patterns.put("Isa",     Pattern.compile("Isaiah",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Josh",    Pattern.compile("Joshua",                   Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Job",     Pattern.compile("Job",                      Pattern.CASE_INSENSITIVE));  
//      patterns.put("Joel",    Pattern.compile("Joel",                     Pattern.CASE_INSENSITIVE));  
//      patterns.put("Jonah",   Pattern.compile("Jonah",                    Pattern.CASE_INSENSITIVE));  
//      patterns.put("Judg",    Pattern.compile("Judges",                   Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Jer",     Pattern.compile("Jeremiah",                 Pattern.CASE_INSENSITIVE));  
//      patterns.put("Lam",     Pattern.compile("Lamentations",             Pattern.CASE_INSENSITIVE));  
//      patterns.put("Lev",     Pattern.compile("Leviticus",                Pattern.CASE_INSENSITIVE));  
//      patterns.put("Mal",     Pattern.compile("Malachi",                  Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Mic",     Pattern.compile("Micah",                    Pattern.CASE_INSENSITIVE));  
//      patterns.put("Nah",     Pattern.compile("Nahum",                    Pattern.CASE_INSENSITIVE));  
//      patterns.put("Neh",     Pattern.compile("Nehemiah",                 Pattern.CASE_INSENSITIVE));  
//      patterns.put("Num",     Pattern.compile("Numbers",                  Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Obad",    Pattern.compile("Obadiah",                  Pattern.CASE_INSENSITIVE));  
//      patterns.put("Prov",    Pattern.compile("Proverbs",                 Pattern.CASE_INSENSITIVE));  
//      patterns.put("Ps",      Pattern.compile("Psalms",                   Pattern.CASE_INSENSITIVE));  
//      patterns.put("Ruth",    Pattern.compile("Ruth",                     Pattern.CASE_INSENSITIVE)); 
//      patterns.put("Song",    Pattern.compile("Song of Songs",            Pattern.CASE_INSENSITIVE));  
//      patterns.put("Zeph",    Pattern.compile("Zephaniah",                Pattern.CASE_INSENSITIVE));  
//      patterns.put("Zech",    Pattern.compile("Zechariah",                Pattern.CASE_INSENSITIVE));  
        
        
        
//        defineAbbreviations("Tobit, 
//      "Tobit", "Tob", "Tb");
//        defineAbbreviations("Judith, 
//      "Jdth", "Jdt", "Jth");
//        defineAbbreviations("Additions to Esther, 
//      "Add Esth", "Add Es", "Rest of Esther", "The Rest of Esther", "AEs", "AddEsth");
//        defineAbbreviations("Wisdom of Solomon, 
//      "Wisd of Sol", "Wis", "Ws", "Wisdom");
//        defineAbbreviations("Sirach, 
//      "Sirach", "Sir", "Ecclesiasticus", "Ecclus");
//        defineAbbreviations("Baruch, 
//      "Baruch", "Bar");
//        defineAbbreviations("Letter of Jeremiah, 
//      "Let Jer", "Let Jer", "LJe", "Ltr Jer");
//        defineAbbreviations("Song of Three Youths, 
//      "Song of Three", "Song Thr", "The Song of Three Youths", "Pr Az", "Prayer of Azariah", "Azariah", "The Song of the Three Holy Children", "The Song of Three Jews", "Song of the Three Holy Children", "Song of Thr", "Song of Three Children", "Song of Three Jews");
//        defineAbbreviations("Susanna, 
//      "Susanna", "Sus");
//        defineAbbreviations("Bel and the Dragon, 
//      "Bel", "Bel");
//        defineAbbreviations("1 Maccabees, 
//      "1 Macc", "1 Mac", "1M", "I Ma", "1Ma", "I Mac", "1Mac", "I Macc", "1Macc", "I Maccabees", "1Maccabees", "1st Maccabees", "First Maccabees");
//        defineAbbreviations("2 Maccabees, 
//      "2 Macc", "2 Mac", "2M", "II Ma", "2Ma", "II Mac", "2Mac", "II Macc", "2Macc", "II Maccabees", "2Maccabees", "2nd Maccabees", "Second Maccabees");
//        defineAbbreviations("1 Esdras, 
//      "1 Esdr", "1 Esd", "I Es", "1Es", "I Esd", "1Esd", "I Esdr", "1Esdr", "I Esdras", "1Esdras", "1st Esdras", "First Esdras");
//        defineAbbreviations("Prayer of Manasseh, 
//      "Pr of Man", "Pr Man", "PMa", "Prayer of Manasses");
//        defineAbbreviations("Additional Psalm, 
//      "Add Psalm", "Add Ps");
//        defineAbbreviations("3 Maccabees, 
//      "3 Macc", "3 Mac", "III Ma", "3Ma", "III Mac", "3Mac", "III Macc", "3Macc", "III Maccabees", "3rd Maccabees", "Third Maccabees");
//        defineAbbreviations("2 Esdras, 
//      "2 Esdr", "2 Esd", "II Es", "2Es", "II Esd", "2Esd", "II Esdr", "2Esdr", "II Esdras", "2Esdras", "2nd Esdras", "Second Esdras");
//        defineAbbreviations("4 Maccabees, 
//      "4 Macc", "4 Mac", "IV Ma", "4Ma", "IV Mac", "4Mac", "IV Macc", "4Macc", "IV Maccabees", "IIII Maccabees", "4Maccabees", "4th Maccabees", "Fourth Maccabees");
//        defineAbbreviations("Ode, 
//      "Ode", "Ode");
//        defineAbbreviations("Psalms of Solomon, 
//      "Ps Solomon", "Ps Sol", "Psalms Solomon", "PsSol");
//        defineAbbreviations("Epistle to the Laodiceans, 
//      "Laodiceans", "Laod", "Ep Laod", "Epist Laodiceans", "Epistle Laodiceans", "Epistle to Laodiceans");
        
        
        patterns.put("1Cor",    Pattern.compile("^[1I]\\s*C\\D*",           Pattern.CASE_INSENSITIVE)); 
        patterns.put("1John",   Pattern.compile("^[1I]\\s*J\\D*",           Pattern.CASE_INSENSITIVE));
        patterns.put("1Pet",    Pattern.compile("^[1I]\\s*P\\D*",           Pattern.CASE_INSENSITIVE));
        patterns.put("1Thess",  Pattern.compile("^[1I]\\s*Th\\D*",          Pattern.CASE_INSENSITIVE));
        patterns.put("1Tim",    Pattern.compile("^[1I]\\s*T[im]\\D*",       Pattern.CASE_INSENSITIVE));
        patterns.put("2Cor",    Pattern.compile("^(?:2|II)\\s*C\\D*",       Pattern.CASE_INSENSITIVE));
        patterns.put("2John",   Pattern.compile("^(?:2|II)\\s*J\\D*",       Pattern.CASE_INSENSITIVE));
        patterns.put("2Pet",    Pattern.compile("^(?:2|II)\\s*P\\D*",       Pattern.CASE_INSENSITIVE)); 
        patterns.put("2Thess",  Pattern.compile("^(?:2|II)\\s*Th\\D*",      Pattern.CASE_INSENSITIVE)); 
        patterns.put("2Tim",    Pattern.compile("^(?:2|II)\\s*T[im]\\D*",   Pattern.CASE_INSENSITIVE));
        patterns.put("3John",   Pattern.compile("^(?:3|III)\\s*J\\D*",      Pattern.CASE_INSENSITIVE)); 
        patterns.put("Acts",    Pattern.compile("^A\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Col",     Pattern.compile("^C\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Eph",     Pattern.compile("^E\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Gal",     Pattern.compile("^G\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Heb",     Pattern.compile("^H\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Jas",     Pattern.compile("^J[mas]\\D*",              Pattern.CASE_INSENSITIVE));
        patterns.put("John",    Pattern.compile("^Jo?[hn]\\D*",             Pattern.CASE_INSENSITIVE));
        patterns.put("Jude",    Pattern.compile("^Ju\\D*",                  Pattern.CASE_INSENSITIVE)); 
        patterns.put("Luke",    Pattern.compile("^L\\D*",                   Pattern.CASE_INSENSITIVE)); 
        patterns.put("Mark",    Pattern.compile("^Ma?[rk]\\D*",             Pattern.CASE_INSENSITIVE));
        patterns.put("Matt",    Pattern.compile("^Ma?t?t\\D*",              Pattern.CASE_INSENSITIVE));
        patterns.put("Phlm",    Pattern.compile("^Ph\\w*m\\D*",             Pattern.CASE_INSENSITIVE));
        patterns.put("Phil",    Pattern.compile("^Ph(il|\\w*p)\\D*",        Pattern.CASE_INSENSITIVE));
        patterns.put("Phil",    Pattern.compile("^Ph\\D*",                  Pattern.CASE_INSENSITIVE));
        patterns.put("Rev",     Pattern.compile("^R[ev]\\D*",               Pattern.CASE_INSENSITIVE));
        patterns.put("Rom",     Pattern.compile("^R[om]\\D*",               Pattern.CASE_INSENSITIVE));
        patterns.put("Titus",   Pattern.compile("^T\\D*",                   Pattern.CASE_INSENSITIVE));

 
    }
    
    static void defineAbbreviations(int ix, String osisBk, String name, String... abbrs) {
        
    }
    public static String parseReference(String ref) {
        // clean the input and split it into segments 
        String[] parts = StringUtils.trimToEmpty(ref).replaceAll("–|—", "-").split("\\s*-+\\s*");
        
        Pattern chapterVersePattern = 
                Pattern.compile("(\\d+)(?:\\D+(\\d+)(?:\\s*,\\s*(\\d+))?)?");
        Matcher matcher = null;
        
        String osisRef = "";
        String startBook = null;
        int startChapter = 0;
        int startVerse = 0;
        
        String endBook = null;
        int endChapter = 0;
        int endVerse = 0;
        
        String vs = null;
        
        // Parse the starting reference
        String firstRef = parts[0];
        for (String bk : patterns.keySet()) {
            Pattern regExp = patterns.get(bk);
            
            matcher = regExp.matcher(firstRef);
            if (matcher.find()) {
                startBook = bk;
                firstRef = firstRef.replace(matcher.group(0), "");
                
                // parse out chapter
                matcher = chapterVersePattern.matcher(firstRef);
                if (matcher.find()) {
                    startChapter = Integer.parseInt(matcher.group(1));
                    
                    // verse
                    vs = matcher.group(2);
                    if (vs != null) {
                        startVerse = Integer.parseInt(matcher.group(2));
                    }
                    
                    // if a comma is separating two verse then honor that
                    if (matcher.groupCount() > 2 && ((vs = matcher.group(3)) != null)) {
                        endBook = startBook;
                        endChapter = startChapter;
                        endVerse = Integer.parseInt(vs);
                        if (endVerse != startVerse + 1)
                            endVerse = 0;
                    }
                }
                
                break;      // found a book for the first reference
            }
        }                   // end for loop
            
        // build reference
        if (startBook == null) {
            return "";
        }
        
        osisRef = startBook;
        if (startChapter != 0)
            osisRef += "." + startChapter;
        if (startVerse != 0)
            osisRef += "." + startVerse;
        
        
        if (parts.length > 1) {
            String secondRef = parts[1];
            
            // look for a book name
            for (String bk : patterns.keySet()) {
                Pattern regExp = patterns.get(bk);
                
                matcher = regExp.matcher(secondRef);
                if (matcher.find()) {
                    endBook = bk;
                    secondRef = secondRef.replace(matcher.group(0), "");
                    
                    break;
                }
            }
            
            // look for ending chapter and verse
            matcher = chapterVersePattern.matcher(secondRef);
            if (matcher.find()) {
                
                String num1 = matcher.group(1);
                String num2 = matcher.group(2);
                
                // if only one number and the ending book isn't specified, this is a verse
                if ((num2 == null) && (endBook == null)) {
                    if (startVerse > 0) {
                        // if a verse is specified in start ref, then the number is a verse
                        endChapter = startChapter;
                        endVerse = Integer.parseInt(num1);
                    } else { 
                        // if only the chapter is specified, then the number is a chapter
                        endChapter = Integer.parseInt(num1);
                    }
                } else {
                    // either the book is supplied or a chapter and verse are supplied
                    // in this case, don't adopt anything from the start ref
                    endChapter = Integer.parseInt(num1);
                    if (num2 != null) {
                        endVerse = Integer.parseInt(num2);
                    }
                }
            }   // done looking for ending chapter and verse
            
            if (endBook == null) {
                endBook = startBook;
            }
        }       // done looking for end reference 
        
        if (endBook != null) {
            osisRef += "-" + endBook;
            if (endChapter > 0) 
                osisRef += "." + endChapter;

            if (endVerse > 0) 
                osisRef += "." + endVerse;
        }
        
        return osisRef;
    }
        
        
//        
//                
//        //If reference is range, parse the ending ref
//        if(@$parts[1]){
//                foreach($ntBookRegExps as $bookRegExp){
//                        if(preg_match($bookRegExp[0], $parts[1])){
//                                $endBook = $bookRegExp[1];
//                                $parts[1] = preg_replace($bookRegExp[0], '', $parts[1]);
//                                break;
//                        }
//                }
//                        
//                //Chapter
//                if(preg_match('/(\d+)(?:\D+(\d+))?/', $parts[1], $chapVerseParts)){
//                        
//                        //If only one number is in ref end, and there is no book specified
//                        if(!@$chapVerseParts[2] && !$endBook){
//                                //If verse specified in start ref, then the number is the verse
//                                if($startVerse){
//                                        $endChapter = $startChapter;
//                                        $endVerse = intval($chapVerseParts[1]);
//                                }
//                                //If only chapter specified, then the number is a chapter
//                                else if($startChapter){
//                                        $endChapter = intval($chapVerseParts[1]);
//                                }
//                        }
//                        //Either the book is supplied or a chapter and verse is supplied; in this case, don't adopt anything from start ref
//                        else {
//                                $endChapter = intval($chapVerseParts[1]);
//                                if(@$chapVerseParts[2])
//                                        $endVerse = intval($chapVerseParts[2]);
//                        }
//                }
//                
//                if(!$endBook)
//                        $endBook = $startBook;
//        }
//
//        if($endBook){
//                $osisref .= '-' . $endBook;
//                if($endChapter)
//                        $osisref .= '.' . $endChapter;
//                if($endVerse)
//                        $osisref .= '.' . $endVerse;
//        }
//        
//        //1 John 3:1,2
//        //1 John 3:2-1 John 3:3
//        //1 John 3-1 John 4
//        //1 John 1:2-1 John 4:2
//        
//        return $osisref;
    
}
