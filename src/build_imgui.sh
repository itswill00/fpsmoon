#!/system/bin/sh
# Build script for Native C++ Dear ImGui ANativeWindow Engine
# Developed & Maintained by @itswill00

cd "${0%/*}"

echo "- Compiling FPSMoon Native C++ Shared Library (libfpsmoon_imgui.so)..."

mkdir -p ../bin
mkdir -p ../build

clang++ -O3 -std=c++20 -shared -fPIC \
  -I./imgui \
  -I./imgui/backends \
  fpsmoon_imgui_native.cpp \
  imgui/imgui.cpp \
  imgui/imgui_draw.cpp \
  imgui/imgui_widgets.cpp \
  imgui/imgui_tables.cpp \
  imgui/backends/imgui_impl_android.cpp \
  imgui/backends/imgui_impl_opengl3.cpp \
  -lEGL -lGLESv3 -landroid -llog \
  -o ../bin/libfpsmoon_imgui.so

if [ $? -ne 0 ]; then
    echo "- Error: Native C++ ImGui shared library compilation failed!"
    exit 1
fi

echo "- Compiling FPSMoon ImGui Surface Launcher DEX..."

ecj -cp /data/data/com.termux/files/usr/share/java/android.jar -d ../build/ FPSMoonImGuiLauncher.java
if [ $? -ne 0 ]; then
    echo "- Error: Java Surface Launcher compilation failed!"
    exit 1
fi

dx --dex --output=../bin/fpsmoon_imgui_launcher.dex ../build/
if [ $? -ne 0 ]; then
    echo "- Error: DEX conversion failed!"
    exit 1
fi

echo "- FPSMoon Native ImGui Engine compiled successfully!"
chmod 755 ../bin/libfpsmoon_imgui.so
