#include "term.h"

#include <string>
#include <list>
#include <vector>
#include <fstream>
#include <stdexcept>
#include <sstream>

TermCollection::TermCollection() {
  collection = std::list<Term>();
}

TermCollection::TermCollection(const std::string &filename) :
  TermCollection() {
  std::ifstream in(filename);
  if (!in) {
    throw(std::invalid_argument(filename));
  }
  std::string line;
  std::string token;

  while (std::getline(in, line)) {
    std::stringstream linestream(line);
    std::vector<std::string> tokens;

    while (std::getline(linestream, token, ',')) {
      tokens.push_back(token);
    }

    Term term = Term(tokens[0], tokens[1], std::stoi(tokens[3]));
    collection.push_back(term);
  }

  collection.sort();
}

unsigned int TermCollection::size() {
  return collection.size();
}

std::list<Term>::iterator TermCollection::begin() {
  return collection.begin();
}

std::list<Term>::iterator TermCollection::end() {
  return collection.end();
}

indri::api::ParsedDocument TermCollection::parse() {
  indri::api::ParsedDocument parsed
}
