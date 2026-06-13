#ifndef PDFREADER_LINK_PARSER_H
#define PDFREADER_LINK_PARSER_H

#include <string>
#include <vector>

namespace pdfreader {

// نوع الرابط المخفي بعد تحليله
enum class HiddenLinkType {
    NONE,           // ليس رابطًا خاصًا
    TTS_WORD,       // رابط نطق كلمة:  tts://word  أو  ##tts:word##
    TTS_SENTENCE,   // رابط نطق جملة:  tts-s://...  أو  ##say:...##
    EXTERNAL_URL,   // رابط خارجي عادي (http/https)
    INTERNAL_REF    // مرجع داخلي ضمن نفس الملف (#page=NN)
};

struct ParsedLink {
    HiddenLinkType type;
    std::string payload;   // النص/الكلمة المراد نطقها أو الرابط الكامل
    std::string langHint;  // تلميح اللغة إن وُجد (مثال: en, fr, de)
};

// يحلل سلسلة URI/Action القادمة من PDFium (PDFAction / URI annotation)
// ويحدد إن كانت رابطًا "مخفيًا" خاصًا بتعلم اللغات أو رابطًا عاديًا.
ParsedLink ParseHiddenLink(const std::string &raw);

// يستخرج جميع الأنماط الشبيهة بالروابط من نص صفحة كامل (احتياطي
// في حال كانت الروابط مكتوبة كنص عادي بدل annotations حقيقية).
std::vector<ParsedLink> ExtractLinksFromText(const std::string &pageText);

}  // namespace pdfreader

#endif // PDFREADER_LINK_PARSER_H
