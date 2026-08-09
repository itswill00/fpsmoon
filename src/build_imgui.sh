#!/system/bin/sh
# Build script for Native C++ Dear ImGui Overlay Engine
# Developed & Maintained by @itswill00

cd "${0%/*}"

echo "- Compiling FPSMoon Native C++ Dear ImGui Engine..."

mkdir -p ../bin

clang++ -O3 -std=c++20 \
  -I./imgui \
  -I./imgui/backends \
  fpsmoon_imgui.cpp \
  imgui/imgui.cpp \
  imgui/imgui_draw.cpp \
  imgui/imgui_widgets.cpp \
  imgui/imgui_tables.cpp \
  imgui/backends/imgui_impl_opengl3.cpp \
  -lEGL -lGLESv3 \
  -o ../bin/fpsmoon_imgui

if [ $? -eq 0 ]; then
    echo "- FPSMoon Native ImGui binary compiled successfully -> bin/fpsmoon_imgui"
    chmod 755 ../bin/fpsmoon_imgui
else
    echo "- Error: Native ImGui compilation failed!"
    exit 1
fi
