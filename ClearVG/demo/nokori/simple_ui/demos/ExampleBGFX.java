package nokori.simple_ui.demos;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import org.lwjgl.glfw.*;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static java.lang.Math.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * bgfx demo.
 *
 * <p>This is a Java port of
 * <a href="https://github.com/memononen/nanovg/blob/master/example/example_gl2.c">https://github.com/memononen/nanovg/blob/master/example/example_gl2.c</a>.</p>
 */
public final class ExampleBGFX extends Demo {

    private ExampleBGFX() {
    }

    private static boolean blowup;
    private static boolean screenshot;
    private static boolean premult;

    public static void main(String[] args) {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW.");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        long window = glfwCreateWindow(1000, 600, "NanoVG (bgfx)", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException();
        }

        glfwSetKeyCallback(window, (windowHandle, keyCode, scancode, action, mods) -> {
            if (keyCode == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(windowHandle, true);
            }
            if (keyCode == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                blowup = !blowup;
            }
            if (keyCode == GLFW_KEY_S && action == GLFW_PRESS) {
                screenshot = true;
            }
            if (keyCode == GLFW_KEY_P && action == GLFW_PRESS) {
                premult = !premult;
            }
        });

        boolean DEMO_MSAA = args.length != 0 && "msaa".equalsIgnoreCase(args[0]);
        glfwGetFramebufferSize(window, fbWidth, fbHeight);

        long vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_ANTIALIAS);

        if (vg == NULL) {
            throw new RuntimeException("Could not init nanovg.");
        }

        DemoData data = new DemoData();
        if (loadDemoData(vg, data) == -1) {
            throw new RuntimeException();
        }

        PerfGraph fps = new PerfGraph();
        initGraph(fps, GRAPH_RENDER_FPS, "Frame Time");

        glfwSetTime(0);
        double prevt = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double t  = glfwGetTime();
            double dt = t - prevt;
            prevt = t;
            updateGraph(fps, (float)dt);

            glfwGetCursorPos(window, mx, my);
            glfwGetWindowSize(window, winWidth, winHeight);
            glfwGetFramebufferSize(window, fbWidth, fbHeight);

            // Calculate pixel ration for hi-dpi devices.
            float pxRatio = fbWidth.get(0) / (float)winWidth.get(0);
            nvgBeginFrame(vg, winWidth.get(0), winHeight.get(0), pxRatio);

            renderDemo(vg, (float)mx.get(0), (float)my.get(0), winWidth.get(0), winHeight.get(0), (float)t, blowup, data);
            renderGraph(vg, 5, 5, fps);

            nvgEndFrame(vg);

            glfwPollEvents();
        }

        freeDemoData(vg, data);

        NanoVGGL3.nvgDelete(vg);

        glfwFreeCallbacks(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private static int RGBA(float r, float g, float b, float a) {
        return
            round(r * 255.0f) << 24 |
            round(g * 255.0f) << 16 |
            round(b * 255.0f) << 8 |
            round(a * 255.0f);
    }

}