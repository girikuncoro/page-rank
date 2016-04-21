"""
Create pre-filtered copy of the input edges file
Usage:
    python process_edges.py <input-file> <output-file>
"""
import sys


NETID = 0.652  # reversed netID of gk256
REJECT_MIN = 0.9 * NETID
REJECT_LIMIT = REJECT_MIN + 0.01

"""
Compute filter parameters, reject about 1% of the edges,
chosen uniformly at random
    @param value from each line of edges.txt
    @return boolean result of rejection
"""
def select_inputline(val):
    if val >= REJECT_MIN and val < REJECT_LIMIT:
        return False
    return True


"""
Filter edges from input_file and write filtered data to new file
    @param input_file, origin file that contains edges
    @param output_file, new file that will contain filtered edges
    @return tuple of count_origin (number of lines in origin file)
            and count_selected (nuber of lines in new file)
"""
def filter_edges(input_file, output_file):
    count_origin = count_selected = 0
    in_file = open(input_file, 'r')
    out_file = open(output_file, 'w')

    for line in in_file:
        source, target, val = line.split()
        count_origin += 1

        if select_inputline(float(val)):
            out_file.write(line)
            count_selected += 1

    in_file.close()
    out_file.close()
    return count_origin, count_selected


"""
Main arguments to run the script in command line
"""
if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]

    print "Begin filtering data..."
    count_origin, count_selected = filter_edges(input_file, output_file)

    print "Successfully rejecting data between {} and {}".format(REJECT_MIN, REJECT_LIMIT)
    print "Number of lines in original file: {}".format(count_origin)
    print "Number of lines in new file: {}".format(count_selected)
