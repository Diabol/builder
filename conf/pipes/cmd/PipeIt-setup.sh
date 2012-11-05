#Create a directory to clone in. $1 resolves to the version of the pipe.
mkdir PipeIt-$1
#Enter the directory and clone PipeIt repository from github.
cd PipeIt-$1/ && git clone git@github.com:Diabol/builder.git
#Remove the below line if you want to always build latest(like if you are testing on your machine)
cd builder && git checkout $2