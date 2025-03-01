all: test

test:
	clj -T:build test

run:
	clojure -M:run-m

deps: prepare
prepare:
	clj -P

.PHONY: prepare test run
