import sys

instr = sys.stdin.readline()[:-1]

if instr[0] == 'v':
    instr = instr[1:]

parts = instr.split('.')

if len(parts) == 3 and parts[2] == '0':
    print(parts[0] + '.' + parts[1])
else:
    print(instr)
