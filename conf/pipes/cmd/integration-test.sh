echo 'CI:Integration-test. Fails sometimes...'

secs=`date +%s`

echo "secs is $secs"

let ${secs}%2
flag=$?

echo "flag is $flag"

if [ $flag -eq 0 ]
then
	echo 'Success...' 
	exit 0
else 
	echo 'Failing...'
	exit 1
fi
