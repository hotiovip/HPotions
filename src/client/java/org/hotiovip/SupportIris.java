package org.hotiovip;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class SupportIris {
    private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";
    private static final String IRIS_PROGRAM_CLASS = "net.irisshaders.iris.api.v0.IrisProgram";
    private static final MethodHandle GET_IRIS_API = getIrisApiHandle();
    private static final MethodHandle REGISTER_PIPELINE = getRegisterPipelineHandle();
    private static final MethodHandle GET_IRIS_PROGRAM = getIrisProgramHandle();

    public static void assignPipelines() {
        assignPipeline(HPotionsClient.LINES_THROUGH_WALLS, "LINES");
    }

    private static void assignPipeline(RenderPipeline pipeline, String irisProgramName) {
        try {
            Objects.requireNonNull(GET_IRIS_API, "Iris API handle must be present to assign a pipeline.");
            Objects.requireNonNull(REGISTER_PIPELINE, "Iris register pipeline handle must be present to assign a pipeline.");
            Objects.requireNonNull(GET_IRIS_PROGRAM, "Iris Program handle must be present to assign a pipeline.");

            REGISTER_PIPELINE.invoke(GET_IRIS_API.invoke(), pipeline, GET_IRIS_PROGRAM.invoke(irisProgramName));
        } catch (IllegalStateException ignored) {
            //The pipeline was probably already registered
        } catch (Throwable e) {
            HPotions.LOGGER.error("[HPotions Iris Compatibility] Failed to assign pipeline {} to {}.", pipeline.getLocation(), irisProgramName, e);
        }
    }

    private static MethodHandle getIrisApiHandle() {
        try {
            Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType type = MethodType.methodType(irisApiClass);

            return lookup.findStatic(irisApiClass, "getInstance", type);
        } catch (Exception e) {
            return null;
        }
    }

    private static MethodHandle getRegisterPipelineHandle() {
        try {
            Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
            Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType type = MethodType.methodType(void.class, RenderPipeline.class, irisProgramClass);

            return lookup.findVirtual(irisApiClass, "assignPipeline", type);
        } catch (Exception e) {
            return null;
        }
    }

    private static MethodHandle getIrisProgramHandle() {
        try {
            Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType type = MethodType.methodType(Enum.class, Class.class, String.class);
            MethodHandle enumValueOf = lookup.findStatic(Enum.class, "valueOf", type);

            return MethodHandles.insertArguments(enumValueOf, 0, irisProgramClass);
        } catch (Exception e) {
            return null;
        }
    }
}
