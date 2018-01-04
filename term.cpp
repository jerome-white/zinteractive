#include "term.h"

#include <string>

Term::Term(std::string name, std::string ngram, int position) {
  this->name = name;
  this->ngram = ngram;
  this->position = position;
}

Term::Term(std::string ngram, int position) : Term(ngram, ngram, position) {}

unsigned int Term::length() const {
  return ngram.length();
}

bool Term::operator<(const Term &rhs) const {
  return (position == rhs.position) ?
    length() < rhs.length() :
    position < rhs.position;
}

bool Term::operator==(const Term &rhs) const {
  return name == rhs.name;
}
