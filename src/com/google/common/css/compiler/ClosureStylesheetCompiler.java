package com.google.common.css.compiler;

import com.google.common.css.DefaultExitCodeHandler;
import com.google.common.css.ExitCodeHandler;
import com.google.common.css.JobDescription;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.commandline.DefaultCommandLineCompiler;

import javax.annotation.Nullable;
import java.io.File;

public class ClosureStylesheetCompiler extends DefaultCommandLineCompiler {

    public ClosureStylesheetCompiler(JobDescription job, ErrorManager errorManager) {
        super(job, new DefaultExitCodeHandler(), errorManager);
    }

    public ClosureStylesheetCompiler(JobDescription job, ExitCodeHandler exitCodeHandler, ErrorManager errorManager) {
        super(job, exitCodeHandler, errorManager);
    }

    @Override
    public String execute(@Nullable File renameFile, @Nullable File sourcemapFile) {
        return super.execute(renameFile, sourcemapFile);
    }
}
