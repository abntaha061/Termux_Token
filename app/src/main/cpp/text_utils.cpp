#include "text_utils.h"
#include <cctype>
#include <sstream>

namespace pdfreader {

std::vector<std::string> SplitIntoWords(const std::string &text) {
    std::vector<std::string> words;
    std::string current;

    size_t i = 0;
    while (i < text.size()) {
        unsigned char c = static_cast<unsigned char>(text[i]);

        // فاصل: مسافة، تبويب، سطر جديد
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            if (!current.empty()) {
                words.push_back(current);
                current.clear();
            }
            i++;
            continue;
        }

        // معالجة UTF-8 متعدد البايت (مثل الحروف العربية): نضيف البايتات كاملة
        if (c < 0x80) {
            current += static_cast<char>(c);
            i++;
        } else {
            int extraBytes = 0;
            if ((c & 0xE0) == 0xC0) extraBytes = 1;
            else if ((c & 0xF0) == 0xE0) extraBytes = 2;
            else if ((c & 0xF8) == 0xF0) extraBytes = 3;

            current += static_cast<char>(c);
            for (int k = 0; k < extraBytes && (i + 1 + k) < text.size(); k++) {
                current += text[i + 1 + k];
            }
            i += 1 + extraBytes;
        }
    }

    if (!current.empty()) {
        words.push_back(current);
    }

    return words;
}

std::string CleanWord(const std::string &word) {
    size_t start = 0;
    size_t end = word.size();

    auto isPunct = [](unsigned char c) {
        return c == '.' || c == ',' || c == '!' || c == '?' || c == ';' ||
               c == ':' || c == '(' || c == ')' || c == '[' || c == ']' ||
               c == '"' || c == '\'' || c == '\xab' || c == '\xbb';
    };

    while (start < end && isPunct(static_cast<unsigned char>(word[start]))) start++;
    while (end > start && isPunct(static_cast<unsigned char>(word[end - 1]))) end--;

    return word.substr(start, end - start);
}

bool ContainsArabic(const std::string &text) {
    // نطاق الحروف العربية في UTF-8 يبدأ بالبايت 0xD8/0xD9 تقريبًا (U+0600-U+06FF)
    for (size_t i = 0; i + 1 < text.size(); i++) {
        unsigned char b1 = static_cast<unsigned char>(text[i]);
        unsigned char b2 = static_cast<unsigned char>(text[i + 1]);
        if (b1 == 0xD8 || b1 == 0xD9) {
            if (b2 >= 0x80 && b2 <= 0xBF) {
                return true;
            }
        }
    }
    return false;
}

}  // namespace pdfreader
