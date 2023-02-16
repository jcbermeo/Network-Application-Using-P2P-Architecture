# Network Data Sharing System with a P2P Architecture

**Authors**: Martin Sendrowicz, Tristen Linnen, Juan C. Bermeo

The following program allows sending and receiving files/data between users using a cetralized server as a directory to keep track of the users in the network and their files.

The program uses a P2P architecture, however, we can call it a hybrid, since a central environment is used, but, just to keep track of the data and the users. 

Some applications have hybrid architectures, combining both Client-Server and P2P (peer to peer) elements. P2P network allows computers (hosts) to communicate directly  with each other without the need for a centralized server. A computer downloads (client side of P2P) or uploads (server side of P2P) a file directly from its peers instead of accessing it from the centralized server. One of the easier to implement P2P architectures is the one that utilizes a Centralized Directory—this is very similar to Napster.

So how does this work?

On the higher level, there is a Centralized Directory server that holds information (index) about each peer. Such information could consist of:
- IP addresses and Port numbers of each peer
- names of files that each peer currently contains

When a given peer joins the network, it contacts the Centralized Directory server where it gets notified about ALL the other peers and the files associated with them. Having that information the peer can now go the specific peer (i.e. the designated owner of the desired file) and start downloading—that is, if the owner of the file is live thus acting as both the server and the client. 
