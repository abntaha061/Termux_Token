#include "link_parser.h"
#include <algorithm>
#include <cctype>
#include <regex>

namespace pdfreader {

// أنماط الروابط المخفية المدعومة:
//   tts://word                -> نطق كلمة واحدة
//   tts:word                  -> نفس الشيء بدون //
//   tts-fr://bonjour          -> نطق مع تحديد اللغة (fr)
//   ##tts:word##              -> صيغة بديلة مضمّنة كنص
//   ##say:a full sentence##   -> نطق جملة كاملة
//   http(s)://...             -> رابط خارجي عادي
//   #page=12                  -> مرجع داخلي لصفحة

static std::string ToLower(const std::string &s) {
    std::string out = s;
    std::transform(out.begin(), out.end(), out.begin(),
                    [](unsigned char c) { return std::tolower(c); });
    return out;
}

static std::string Trim(const std::string &s) {
    size_t start = s.find_first_not_of(" \t\n\r");
    if (start == std::string::npos) return "";
    size_t end = s.find_last_not_of(" \t\n\r");
    return s.substr(start, end - start + 1);
}

ParsedLink ParseHiddenLink(const std::string &raw) {
    ParsedLink result;
    result.type = HiddenLinkType::NONE;

    std::string s = Trim(raw);
    if (s.empty()) return result;

    std::string lower = ToLower(s);

    // 1) tts-XX://payload  أو tts://payload  أو tts:payload
    static const std::regex ttsLangRe(R"(^tts-([a-zA-Z]{2,5})://(.*)$)");
    static const std::regex ttsRe(R"(^tts://(.*)$)");
    static const std::regex ttsColonRe(R"(^tts:(.*)$)");

    std::smatch m;
    if (std::regex_match(s, m, ttsLangRe)) {
        result.type = HiddenLinkType::TTS_WORD;
        result.langHint = m[1].str();
        result.payload = Trim(m[2].str());
        return result;
    }
    if (std::regex_match(s, m, ttsRe)) {
        result.type = HiddenLinkType::TTS_WORD;
        result.payload = Trim(m[1].str());
        return result;
    }
    if (std::regex_match(s, m, ttsColonRe)) {
        result.type = HiddenLinkType::TTS_WORD;
        result.payload = Trim(m[1].str());
        return result;
    }

    // 2) ##tts:word## أو ##say:sentence##
    static const std::regex hashTtsRe(R"(^##tts:(.*)##$)");
    static const std::regex hashSayRe(R"(^##say:(.*)##$)");
    if (std::regex_match(s, m, hashTtsRe)) {
        result.type = HiddenLinkType::TTS_WORD;
        result.payload = Trim(m[1].str());
        return result;
    }
    if (std::regex_match(s, m, hashSayRe)) {
        result.type = HiddenLinkType::TTS_SENTENCE;
        result.payload = Trim(m[1].str());
        return result;
    }

    // 3) مرجع داخلي لصفحة: #page=12 أو #12
    static const std::regex pageRefRe(R"(^#(?:page=)?(\d+)$)");
    if (std::regex_match(s, m, pageRefRe)) {
        result.type = HiddenLinkType::INTERNAL_REF;
        result.payload = m[1].str();
        return result;
    }

    // 4) رابط خارجي عادي
    if (lower.rfind("http://", 0) == 0 || lower.rfind("https://", 0) == 0) {
        result.type = HiddenLinkType::EXTERNAL_URL;
        result.payload = s;
        return result;
    }

    return result;
}

std::vector<ParsedLink> ExtractLinksFromText(const std::string &pageText) {
    std::vector<ParsedLink> links;

    // البحث عن الأنماط ##tts:...##  و ##say:...##  المضمّنة داخل النص
    static const std::regex embeddedRe(R"((##(?:tts|say):[^#]*##))");
    auto begin = std::sregex_iterator(pageText.begin(), pageText.end(), embeddedRe);
    auto end = std::sregex_iterator();

    for (auto it = begin; it != end; ++it) {
        std::string match = it->str();
        ParsedLink parsed = ParseHiddenLink(match);
        if (parsed.type != HiddenLinkType::NONE) {
            links.push_back(parsed);
        }
    }

    // البحث عن روابط http/https خام داخل النص
    static const std::regex urlRe(R"((https?://[^\s\)\]]+))");
    auto ubegin = std::sregex_iterator(pageText.begin(), pageText.end(), urlRe);
    for (auto it = ubegin; it != end; ++it) {
        ParsedLink parsed;
        parsed.type = HiddenLinkType::EXTERNAL_URL;
        parsed.payload = it->str();
        links.push_back(parsed);
    }

    return links;
}

}  // namespace pdfreader
