#ifndef PDFREADER_TEXT_UTILS_H
#define PDFREADER_TEXT_UTILS_H

#include <string>
#include <vector>

namespace pdfreader {

// يقسم نصًا إلى كلمات مفردة بناءً على المسافات وعلامات الترقيم،
// مع دعم النصوص العربية والـ Unicode (UTF-8).
std::vector<std::string> SplitIntoWords(const std::string &text);

// يزيل علامات الترقيم الزائدة من بداية/نهاية الكلمة (لتحسين دقة النطق
// عند الضغط على كلمة محددة من النص المُحدد).
std::string CleanWord(const std::string &word);

// يتحقق إن كان النص يحتوي على حروف عربية (لتحديد لغة النطق المناسبة
// تلقائيًا إذا لم يوجد langHint).
bool ContainsArabic(const std::string &text);

}  // namespace pdfreader

#endif // PDFREADER_TEXT_UTILS_H
