# HuffmanCode
The Huffman Code base on the binary tree.use a priority queue(base on binary tree) to place all the 8 bits char and its frequency in order(base on the frequency).  
then we use a binary tree to store the char map. Theory shows that using a resizeable length of char coding takes less space.  
every char has it`s unique key, which depend of the frequency of the whole map. we use the edge of the tree to indicate the bits of the key.  
we use the edge that links the father node and left child node to indicate 0,the other indicates 1.
when we scan a each bit of the file stream, we follow the principle that if we meet 0,turn to the left child, otherwise turn to the right child
