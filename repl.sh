for jar in libs/*.jar; do
		echo "[INFO] : Adding $jar to the CLASSPATH ..."
		if [ -z $CLASSPATH ]; then
			export CLASSPATH="$jar"
		else
			export CLASSPATH="$jar:$CLASSPATH"
		fi
	done

# List in the CLASSPATH
echo -e "[INFO] : CLASSPATH : ${CLASSPATH}"

exec java -cp ${CLASSPATH} javarepl.Main
