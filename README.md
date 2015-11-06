# LogTool
Quick and dirty code scanner for locate logs entries

Siply use the main class passing as parameters the base directories of project you are intended to analyze, the result is saved as CSV format into a the remporary file prompted after the execution. The LogTool generate a CSV containing all accurences of log invocation per Class.  LogToolPackage scan the applicative log and generate a CSV containing the frequency of log writing per class, you need to specify the package of your project.

    use like $java it.loreii.utils.log.LogTool src_dirA src_dirB ...
    use like $java it.loreii.utils.log.LogToolPackage org.example log_dirA log_dirB ...

  
