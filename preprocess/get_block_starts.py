def block_start():
	in_f = open("data/blocks.txt", "r")
	out_f = open("data/block_start.txt", "w")
	in_read = in_f.read().splitlines()

	start = 0

	for i,line in enumerate(in_read):
		end = start + int(line) - 1
		out_f.write("Block {} starts at node {} ends at {}\n".format(i, start, end))
		start = end + 1

	in_f.close()
	out_f.close()


if __name__ == "__main__":
	block_start()