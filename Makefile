ROUTINE?="fighting"

all: test

test:
	clj -T:build test

run:
	clojure -M:run-m :prefer-routine "$(ROUTINE)"

deps: prepare
prepare:
	clj -P

.PHONY: prepare test run
