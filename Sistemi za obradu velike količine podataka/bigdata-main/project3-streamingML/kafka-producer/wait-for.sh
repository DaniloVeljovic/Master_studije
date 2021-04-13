if [[ ! $# -eq 2 ]]; then
  echo "Expect two arguments: api end point to wait for and callback script name"
  exit 1
fi

while ! curl --output /dev/null --silent --head --fail "$1"
do
    sleep 5
done

sh -c "$2"