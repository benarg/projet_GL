import os
import sys
from threading import Thread

os.chdir("../../../")

contextPath = "src/test/deca/context"
validFilesPath = "src/test/deca/syntax/valid/our_tests"
invalidFilesPath = "src/test/deca/context/invalid/our_tests"
validFileResultsPath = "src/test/deca/context/valid/cont-results"
invalidFileResultsPath = "src/test/deca/context/invalid/cont-results"

launcherPath = "src/test/script/launchers"
scriptPath = "src/test/script"

testResultsPath = "src/test/script/testResults"

isDebug = False
isValid = False
isInvalid = False
cobertura = False

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
    if (i == "-c"):
        cobertura = True

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

i=0
while i<len(validFiles) :
    f = validFiles[i]
    if (len(f.split('.')) != 2):
        validFiles.pop(i)
    elif (f.split('.')[1] != 'deca'):
        validFiles.pop(i)
    else:
        i+=1

i=0
while i<len(invalidFiles) :
    f = invalidFiles[i]
    if (len(f.split('.')) != 2):
        invalidFiles.pop(i)
    elif (f.split('.')[1] != 'deca'):
        invalidFiles.pop(i)
    else:
        i+=1

## execute laucher on each file and save each result in
## testResults directory

def checkTest(isValid, testName, results, index):
    testName = testName.split('.')[0]
    testResult = testName + "_result.txt"
    testPath = ""
    expectedResultPath = ""
    resultPath = testResultsPath + "/" + testResult
    if (isValid):
        testPath = validFilesPath + "/" + testName + ".deca"
        expectedResultPath = validFileResultsPath + "/" + testResult
    else:
        testPath = invalidFilesPath + "/" + testName + ".deca"
        expectedResultPath = invalidFileResultsPath + "/" + testResult

    os.system("./" + launcherPath + "/test_context " + 
            testPath + " > " + resultPath + " 2>&1")

    if (isDebug):

        if (os.system("diff -b " + expectedResultPath + " " + resultPath)):
            results[index] = False
            print("######   FAIL: " + testName  + " ######")
        else:
            results[index] = True
            print("######   PASS: " + testName  + " ######")

    else:

        os.system("diff -b " + expectedResultPath + " " + resultPath + " > " + testResultsPath + "/" + testName + "_diff.txt")
        
        if (os.path.getsize(testResultsPath + "/" + testName + "_diff.txt") == 0):
            results[index] = True
        else:
            results[index] = False


if (isValid):
    results = [None] * len(validFiles)

    if (cobertura):
        for i in range(len(validFiles)):
            checkTest(True, validFiles[i], results, i)
    else:
        threads = [None] * len(validFiles)

        for i in range(len(validFiles)):
            threads[i] = Thread(target=checkTest, args=(True, validFiles[i], results, i))
            threads[i].start()

        for i in range(len(threads)):
            threads[i].join()
    
    for i in range(len(validFiles)):
        if results[i]:
            numPass += 1
        else:
            numFail += 1
            failedTests.append(validFiles[i])

if (isInvalid):

    results = [None] * len(invalidFiles)

    if (cobertura):
        for i in range(len(invalidFiles)):
            checkTest(False, invalidFiles[i], results, i)

    else:
        threads = [None] * len(invalidFiles)

        for i in range(len(invalidFiles)):
            threads[i] = Thread(target=checkTest, args=(False, invalidFiles[i], results, i))
            threads[i].start()

        for i in range(len(threads)):
            threads[i].join()
    
    for i in range(len(invalidFiles)):
        if results[i]:
            numPass += 1
        else:
            numFail += 1
            failedTests.append(invalidFiles[i])


print("[CONT] PASSED TESTS : ", numPass)
print("[CONT] FAILED TESTS : ", numFail)

if (not(isDebug)):

    print("[CONT] THE FAILED TESTS ARE : ", failedTests)