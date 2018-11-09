import os
import sys

##########  GLOBAL VARIABLES  ##########  

scriptPath = "src/test/script"
launcherPath = scriptPath + "/launchers"
validFilesPath = "src/test/deca/syntax/valid/our_tests"

lexInvalidFilesPath = "src/test/deca/syntax/invalid/our_lex_tests"
lexValidFileResultsPath = "src/test/deca/syntax/valid/lex-results"
lexInvalidFileResultsPath = "src/test/deca/syntax/invalid/lex-results"

syntInvalidFilesPath = "src/test/deca/syntax/invalid/our_synt_tests"
syntValidFileResultsPath = "src/test/deca/syntax/valid/synt-results"
syntInvalidFileResultsPath = "src/test/deca/syntax/invalid/synt-results"

contInvalidFilesPath = "src/test/deca/context/invalid/our_tests"
contValidFileResultsPath = "src/test/deca/context/valid/cont-results"
contInvalidFileResultsPath = "src/test/deca/context/invalid/cont-results"

exeInvalidFilesPath = "src/test/deca/codegen/invalid/our_tests"
exeValidFileResultsPath = "src/test/deca/codegen/valid/exe-results"
exeInvalidFileResultsPath = "src/test/deca/codegen/invalid/exe-results"

# Pour le ByteCode
exeBCInvalidFilesPath = "src/test/deca/bytegen/invalid/our_tests"
exeBCValidFileResultsPath = "src/test/deca/bytegen/valid/bc-results"
exeBCInvalidFileResultsPath = "src/test/deca/bytegen/invalid/bc-results"

##########  FUNCTION DEFINITIONS  ##########  

        ##########  PARSE ARGS FOR TEST CREATION  ##########

def parse_create_test():

    validType = ''
    testType = ''

    print("")
    print("What type of test is this?")
    
    arg = raw_input("[VALID/INVALID]? ")
    
    while (arg != "VALID" and arg != "INVALID"):
        print("you have entered an incorrect argument. Please type VALID or INVALID.")
        arg = raw_input("[VALID/INVALID]? ")

    validType = arg

    if (validType == "INVALID"):
        print("")
        print("What type of test is this?")

        arg = raw_input("[LEX/SYNT/CONT/DECA]? ")
        
        while (arg != "LEX" and arg != "SYNT" and arg != "CONT" and arg != "DECA"):
            print("you have entered an incorrect argument. Please type LEX or SYNT or CONT or DECA.")
            arg = raw_input("[LEX/SYNT/CONT/DECA]? ")

        testType = arg
    else:
        testType = ''
    
    create_test(validType, testType)

    ##########  TEST CREATION  ##########

def create_test(validType, testType):

    invalidFilesPath = ''
    validFileResultsPath = ''
    invalidFileResultsPath = ''
    launcher = ''

    if (validType == "INVALID"):
        if (testType == "LEX"):
            invalidFilesPath = lexInvalidFilesPath
            validFileResultsPath = lexValidFileResultsPath
            invalidFileResultsPath = lexInvalidFileResultsPath
            launcher = "test_lex"
        if (testType == "SYNT"):
            invalidFilesPath = syntInvalidFilesPath
            validFileResultsPath = syntValidFileResultsPath
            invalidFileResultsPath = syntInvalidFileResultsPath
            launcher = "test_synt"
        if (testType == "CONT"):
            invalidFilesPath = contInvalidFilesPath
            validFileResultsPath = contValidFileResultsPath
            invalidFileResultsPath = contInvalidFileResultsPath
            launcher = "test_context"
        if (testType == "DECA"):
            print("NOT YET IMPLEMENTED")

    print(" ")
    print("Please enter the name of the test (without .deca)")
    
    arg = raw_input("Test name : ")

    filename = arg + ".deca"
    
    newTestFile = open(filename, "w")

    print(" ")
    print("Please enter some DECA CODE as well as the DESCRIPTION.")
    print("Enter EOF when you are done.")

    buffer = []
    done = False

    while not(done):
        line = raw_input()
        if (line == "EOF"):
            break
        if (line != ''):
            line = line.split("\t")
            if (line[0] == ''):
                line = line[1]
            else:
                line = line[0]
        buffer.append(line)
    
    for line in buffer:
        newTestFile.write("%s\n" % line)
 
    newTestFile.close()

    if (validType == "VALID"):

        imaResult = filename.split('.')[0] + "_result.txt"
        os.system("./" + launcherPath + "/test_lex " + " " + 
                filename + " > " + lexValidFileResultsPath
                + "/" + imaResult + " 2>&1")
        os.system("./" + launcherPath + "/test_synt " + " " + 
                filename + " > " + syntValidFileResultsPath
                + "/" + imaResult + " 2>&1")
        os.system("./" + launcherPath + "/test_context " + " " + 
                filename + " > " + contValidFileResultsPath
                + "/" + imaResult + " 2>&1")

        assResult = filename.split('.')[0] + ".ass"
        os.system("./src/main/bin/decac " + filename)
        os.system("ima " + assResult + " > " + exeValidFileResultsPath + "/" + imaResult + " 2>&1")
        os.system("rm " + assResult)

        # Pour le ByteCode
        javaResult = filename.split('.')[0] + "_byte_result.txt"
        classResult = filename.split('.')[0]
        classResultDot = classResult + ".class"
        os.system("./src/main/bin/decac -B " + filename)
        os.system("java " + classResult + " > " + exeBCValidFileResultsPath + "/" + javaResult + " 2>&1")
        os.system("rm " + classResultDot)

        os.system("mv " + filename + " " + validFilesPath)

    if (validType == "INVALID"):

        if (testType != "DECA"):

            os.system("mv " + filename + " " + invalidFilesPath)

            testResult = filename.split('.')[0] + "_result.txt"
            os.system("./" + launcherPath + "/" + launcher + " " 
                    + invalidFilesPath + "/" + filename + " > " 
                    + invalidFileResultsPath + "/" + testResult + " 2>&1")

        if (testType == "DECA"):

            imaResult = filename.split('.')[0] + "_result.txt"

            assResult = filename.split('.')[0] + ".ass"
            os.system("./src/main/bin/decac " + filename)
            os.system("ima " + assResult + " > " + exeInvalidFileResultsPath + "/" + imaResult + " 2>&1")
            os.system("rm " + assResult)
            os.system("cp " + filename + " " + exeInvalidFilesPath)

            # Pour le ByteCode
            javaResult = filename.split('.')[0] + "_byte_result.txt"
            classResult = filename.split('.')[0]
            classResultDot = classResult + ".class"
            os.system("./src/main/bin/decac -B " + filename)
            os.system("java " + classResult + " > " + exeBCInvalidFileResultsPath + "/" + javaResult + " 2>&1")
            os.system("rm " + classResultDot)
            os.system("mv " + filename + " " + exeBCInvalidFilesPath)          


##########  MAIN PROGRAM  ########## 

def main():

    print("  ")
    print("############################################### ")
    print("###########   PYTHON TEST MANAGER   ########### ")
    print("############################################### ")
    print("  ")
    print("Welcome to the python test manager !!!!")
    print("With this test manager you can : ")
    print("  - execute all tests ")
    print("  - execute specific tests ")
    print("  - create new tests ")
    print("  ")

    print("Would you like to CREATE or EXECUTE tests ?")
    print("  ")

    arg = raw_input("[CREATE/EXEC]? ")

    while (arg != "CREATE" and arg != "EXEC"):
        print("You have entered an incorrect argument. Please type CREATE or EXEC.")
        arg = raw_input("[CREATE/EXEC]? ")

    if (arg == "CREATE"):
        parse_create_test()
    else:
        exec_tests()

os.chdir("../../../")
main()