import os
import sys
from threading import Thread

os.chdir("../../../")

validFilesPath = "src/test/deca/syntax/valid/our_tests"
vResultsPath = "src/test/deca/comp_options/our_results/vOption"
rResultsPath = "src/test/deca/comp_options/our_results/rOption"
bResultsPath = "src/test/deca/comp_options/our_results/bOption"
nResultsPath = "src/test/deca/comp_options/our_results/nOption"
dResultsPath = "src/test/deca/comp_options/our_results/vOption"
launcherPath = "src/test/script/launchers"
testResultsPath = "src/test/script/testResults"

vOption = False
pOption = False
rOption = False
bOption = False
nOption = False
dOption = False
cOption = False
debugMode = False

numPass = 0
numFail = 0

for i in sys.argv:
    if (i == "-v"):
        vOption = True
    if (i == "-p"):
        pOption = True
    if (i == "-r"):
        rOption = True
    if (i == "-b"):
        bOption = True
    if (i == "-n"):
        nOption = True
    if (i == "-d"):
        dOption = True
    if (i == "-c"):
        cOption = True
    if (i == "-debug"):
        debugMode = True

options = ["-v", "-p", "-r", "-b", "-n", "-d"]

if (os.path.isdir(testResultsPath)):
    os.system("rm -rf " + testResultsPath)
    os.system("mkdir " + testResultsPath)
else:
    os.system("mkdir " + testResultsPath)

validFiles = os.listdir(validFilesPath)

i=0
while i<len(validFiles) :
    f = validFiles[i]
    if (len(f.split('.')) != 2):
        validFiles.pop(i)
    elif (f.split('.')[1] != 'deca'):
        validFiles.pop(i)
    else:
        i+=1

## test option : -b ##

if (bOption):

    bFile1 = bResultsPath + "/optionB.txt"
    bFile2 = testResultsPath + "/optionB.txt"
    os.system("./src/main/bin/decac -b > " + bFile2)

    if (os.system("diff -b " + bFile1 + " " + bFile2)):
        print("###### OPTION -b FAIL ######")
    else:
        print("###### OPTION -b PASS ######")

## test option : -p ##

if (pOption):
    def checkParse(file, results, index):
        result1 = file.split('.')[0] + "_result.deca"
        result2 = file.split('.')[0] + "_result2.deca"
        os.system("./src/main/bin/decac -p " + validFilesPath + "/" + file
                + " > " + testResultsPath + "/" + result1)
        os.system("./src/main/bin/decac -p " + testResultsPath + "/" 
                + result1 + " > " + testResultsPath + "/" + result2)
        if (os.system("diff -b " + testResultsPath + "/" + result1 + " " 
                    + testResultsPath + "/" + result2)):
            if (debugMode):
                print("###### OPTION -p FAIL : " + result1 + " ######")
            results[index] = False
        else:
            if (debugMode):
                print("###### OPTION -p PASS : " + result1 + " ######")
            results[index] = True

    results = [None] * len(validFiles)

    if (cOption):
        for i in range(len(validFiles)):
            checkParse(validFiles[i], results, i)
    else:
        threads = [None] * len(validFiles)
        for i in range(len(validFiles)):
            threads[i] = Thread(target=checkParse, args=(validFiles[i], results, i))
            threads[i].start()

        for i in range(len(threads)):
            threads[i].join()
    
    for res in results:
        if res:
            numPass += 1
        else:
            numFail += 1


    print("[Option -p] PASSED TESTS : ", numPass)
    print("[Option -p] FAILED TESTS : ", numFail)
 