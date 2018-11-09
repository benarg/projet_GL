import os
import sys

os.chdir("../../../")

exePath = "src/test/deca/codegen"
validFilesPath = "src/test/deca/syntax/valid/our_tests"
invalidFilesPath = "src/test/deca/codegen/invalid/our_tests"
validFileResultsPath = "src/test/deca/codegen/valid/exe-results"
invalidFileResultsPath = "src/test/deca/codegen/invalid/exe-results"
validBCFileResultsPath = "src/test/deca/bytegen/valid/bc-results"
invalidBCFileResultsPath = "src/test/deca/bytegen/invalid/bc-results"

launcherPath = "src/test/script/launchers"
scriptPath = "src/test/script"

testResultsPath = "src/test/script/testResults"

isDebug = False
isValid = False
isInvalid = False

numPass = 0
numFail = 0

failedTests = []

for i in sys.argv:
    if (i == "-v"):
        isValid = True
    if (i == "-i"):
        isInvalid = True
    if (i == "-d"):
        isDebug = True

if (not(isDebug) and not(isInvalid) and not(isValid)):
    isValid = True
    isInvalid = True
    isDebug = True

## re(create) testResults

if (os.path.isdir(testResultsPath)):
    os.system("rm -rf " + testResultsPath)
    os.system("mkdir " + testResultsPath)
else:
    os.system("mkdir " + testResultsPath)

## create list of deca files to test

validFiles = os.listdir(validFilesPath)
invalidFiles = os.listdir(invalidFilesPath)

cmd1 = cmd2 = cmd3 = "./src/main/bin/decac -P "
cmd3 += "-B "
cmd4 = cmd3

i=0
while i<len(validFiles) :
    f = validFiles[i]
    if (len(f.split('.')) != 2):
        validFiles.pop(i)
    elif (f.split('.')[1] != 'deca'):
        validFiles.pop(i)
    else:
        cmd1 = cmd1 + validFilesPath + "/" + f + " "
        cmd3 = cmd3 + validFilesPath + "/" + f + " "
        i+=1

i=0
while i<len(invalidFiles) :
    f = invalidFiles[i]
    if (len(f.split('.')) != 2):
        invalidFiles.pop(i)
    elif (f.split('.')[1] != 'deca'):
        invalidFiles.pop(i)
    else:
        cmd2 = cmd2 + invalidFilesPath + "/" + f + " "
        cmd4 = cmd4 + invalidFilesPath + "/" + f + " "
        i+=1


## execute laucher on each file and save each result in
## testResults directory

if (isValid):

    os.system(cmd1 + " > " + testResultsPath + "/junk.txt 2>&1")
    os.system(cmd3 + " > " + testResultsPath + "/junk.txt 2>&1")

    for f in validFiles:
        testName = f.split('.')[0]
        assResult = testName + ".ass"
        classResult = testName + ".class"
        imaResult = testName + "_result.txt"
        javaResult = testName + "_byte_result.txt"
        os.system("ima " + validFilesPath + "/" + assResult + " > " + 
                    testResultsPath + "/" + imaResult + " 2>&1")
        os.system("java " + "-cp " + validFilesPath + " " + testName + " > " + 
                    testResultsPath + "/" + javaResult + " 2>&1")
        os.system("rm " + validFilesPath + "/" + assResult + " 2> "+ testResultsPath + "/junk.txt")
        os.system("rm " + validFilesPath + "/" + classResult + " 2> "+ testResultsPath + "/junk.txt")

        f1 = testResultsPath + "/" + imaResult
        f2 = validFileResultsPath + "/" + imaResult
        f3 = testResultsPath + "/" + javaResult
        f4 = validBCFileResultsPath + "/" + javaResult
	
        
        if (isDebug):

            if (os.system("diff -b " + f1 + " " + f2)):
                numFail = numFail + 1
                print("######   FAIL: " + imaResult  + " ######")
            else:
                numPass = numPass + 1
                print("######   PASS: " + imaResult  + " ######")
	    
	    if (os.system("diff -b " + f3 + " " + f4)):
                numFail = numFail + 1
                print("######   FAIL: " + javaResult  + " ######")
            else:
                numPass = numPass + 1
                print("######   PASS: " + javaResult  + " ######")

        else:

            os.system("diff -b " + f1 + " " + f2 + " > " + testResultsPath + "/diff.txt")
                
            if (os.path.getsize(testResultsPath + "/diff.txt") == 0):
                numPass = numPass + 1
            else:
                numFail = numFail + 1
                failedTests.append(imaResult)

            os.system("diff -b " + f3 + " " + f4 + " > " + testResultsPath + "/diff.txt")
                
            if (os.path.getsize(testResultsPath + "/diff.txt") == 0):
                numPass = numPass + 1
            else:
                numFail = numFail + 1
                failedTests.append(javaResult)

            os.system("rm " + testResultsPath + "/diff.txt" + " 2> "+ testResultsPath + "/junk.txt")



if (isInvalid):

    os.system(cmd2 + " > " + testResultsPath + "/junk.txt 2>&1")
    os.system(cmd4 + " > " + testResultsPath + "/junk.txt 2>&1")

    for f in invalidFiles:
        testName = f.split('.')[0]
        assResult = testName + ".ass"
        classResult = testName + ".class"
        imaResult = testName + "_result.txt"
        javaResult = testName + "_byte_result.txt"
        os.system("ima " + invalidFilesPath + "/" + assResult + " > " + 
                    testResultsPath + "/" + imaResult + " 2>&1")
        os.system("java " + "-cp " + invalidFilesPath + " " + testName + " > " + 
                    testResultsPath + "/" + javaResult + " 2>&1")
        os.system("rm " + invalidFilesPath + "/" + assResult + " 2> "+ testResultsPath + "/junk.txt")
        os.system("rm " + invalidFilesPath + "/" + classResult + " 2> "+ testResultsPath + "/junk.txt")

        f1 = testResultsPath + "/" + imaResult
        f2 = invalidFileResultsPath + "/" + imaResult
        f3 = testResultsPath + "/" + javaResult
        f4 = invalidBCFileResultsPath + "/" + javaResult

        if (isDebug):

            if (os.system("diff -b " + f1 + " " + f2)):
                numFail = numFail + 1
                print("######   FAIL: " + imaResult  + " ######")
            else:
                numPass = numPass + 1
                print("######   PASS: " + imaResult  + " ######")
        
            if (os.system("diff -b " + f3 + " " + f4)):
                numFail = numFail + 1
                print("######   FAIL: " + javaResult  + " ######")
            else:
                numPass = numPass + 1
                print("######   PASS: " + javaResult  + " ######")

        else:

            os.system("diff -b " + f1 + " " + f2 + " > " + testResultsPath + "/diff.txt")
                
            if (os.path.getsize(testResultsPath + "/diff.txt") == 0):
                numPass = numPass + 1
            else:
                numFail = numFail + 1
                failedTests.append(imaResult)

            os.system("diff -b " + f3 + " " + f4 + " > " + testResultsPath + "/diff.txt")
                
            if (os.path.getsize(testResultsPath + "/diff.txt") == 0):
                numPass = numPass + 1
            else:
                numFail = numFail + 1
                failedTests.append(javaResult)

            os.system("rm " + testResultsPath + "/diff.txt" + " 2> "+ testResultsPath + "/junk.txt")

print("[EXE] PASSED TESTS : ", numPass)
print("[EXE] FAILED TESTS : ", numFail)

if (not(isDebug)):

    print("[EXE] THE FAILED TESTS ARE : ", failedTests)
