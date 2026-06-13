cmake_minimum_required(VERSION 3.22.1)
project("pdfreader")

# مكتبة C++ مساعدة:
# - تحليل أنماط الروابط المخفية الخاصة بالنطق (مثل: tts://word أو ##audio:word##)
# - معالجة النصوص (تطبيع، تقسيم، استخراج كلمات)
# هذه الوظائف خفيفة وسريعة، ونفّذناها في C++ لتسريع المعالجة على المستندات الكبيرة (200 صفحة).

add_library(pdfreader_native SHARED
        link_parser.cpp
        text_utils.cpp
        native_bridge.cpp)

find_library(log-lib log)

target_link_libraries(pdfreader_native ${log-lib})

target_compile_options(pdfreader_native PRIVATE -O2 -Wall)
