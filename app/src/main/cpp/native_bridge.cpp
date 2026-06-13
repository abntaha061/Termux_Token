#include <jni.h>
#include <string>
#include <vector>
#include "link_parser.h"
#include "text_utils.h"

using namespace pdfreader;

// تحويل ParsedLink إلى مصفوفة سلاسل Java: [type, payload, langHint]
static jobjectArray ParsedLinkToJavaArray(JNIEnv *env, const ParsedLink &link) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(3, stringClass, nullptr);

    std::string typeStr;
    switch (link.type) {
        case HiddenLinkType::TTS_WORD: typeStr = "TTS_WORD"; break;
        case HiddenLinkType::TTS_SENTENCE: typeStr = "TTS_SENTENCE"; break;
        case HiddenLinkType::EXTERNAL_URL: typeStr = "EXTERNAL_URL"; break;
        case HiddenLinkType::INTERNAL_REF: typeStr = "INTERNAL_REF"; break;
        default: typeStr = "NONE"; break;
    }

    env->SetObjectArrayElement(result, 0, env->NewStringUTF(typeStr.c_str()));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(link.payload.c_str()));
    env->SetObjectArrayElement(result, 2, env->NewStringUTF(link.langHint.c_str()));

    return result;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_pdfreader_native_NativeLib_parseHiddenLink(
        JNIEnv *env, jobject /* this */, jstring rawUri) {
    const char *cstr = env->GetStringUTFChars(rawUri, nullptr);
    std::string raw(cstr);
    env->ReleaseStringUTFChars(rawUri, cstr);

    ParsedLink link = ParseHiddenLink(raw);
    return ParsedLinkToJavaArray(env, link);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_pdfreader_native_NativeLib_extractLinksFromText(
        JNIEnv *env, jobject /* this */, jstring pageText) {
    const char *cstr = env->GetStringUTFChars(pageText, nullptr);
    std::string text(cstr);
    env->ReleaseStringUTFChars(pageText, cstr);

    std::vector<ParsedLink> links = ExtractLinksFromText(text);

    jclass stringArrayClass = env->FindClass("[Ljava/lang/String;");
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(links.size()), stringArrayClass, nullptr);

    for (size_t i = 0; i < links.size(); i++) {
        jobjectArray entry = ParsedLinkToJavaArray(env, links[i]);
        env->SetObjectArrayElement(result, static_cast<jsize>(i), entry);
        env->DeleteLocalRef(entry);
    }

    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_pdfreader_native_NativeLib_cleanWord(
        JNIEnv *env, jobject /* this */, jstring word) {
    const char *cstr = env->GetStringUTFChars(word, nullptr);
    std::string w(cstr);
    env->ReleaseStringUTFChars(word, cstr);

    std::string cleaned = CleanWord(w);
    return env->NewStringUTF(cleaned.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_pdfreader_native_NativeLib_containsArabic(
        JNIEnv *env, jobject /* this */, jstring text) {
    const char *cstr = env->GetStringUTFChars(text, nullptr);
    std::string t(cstr);
    env->ReleaseStringUTFChars(text, cstr);

    return ContainsArabic(t) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_pdfreader_native_NativeLib_splitIntoWords(
        JNIEnv *env, jobject /* this */, jstring text) {
    const char *cstr = env->GetStringUTFChars(text, nullptr);
    std::string t(cstr);
    env->ReleaseStringUTFChars(text, cstr);

    std::vector<std::string> words = SplitIntoWords(t);

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(words.size()), stringClass, nullptr);

    for (size_t i = 0; i < words.size(); i++) {
        env->SetObjectArrayElement(result, static_cast<jsize>(i), env->NewStringUTF(words[i].c_str()));
    }

    return result;
}
