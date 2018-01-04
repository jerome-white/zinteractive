#ifndef TERM_H
#define TERM_H

#include <string>
#include <list>

class Term {
 private:
  unsigned int position;
  unsigned int span;
  
  std::string name;
  std::string ngram;

 public:
  Term(std::string, std::string, int);
  Term(std::string, int);
  
  unsigned int length() const;
  // friend bool operator<(const Term &, const Term &);
  bool operator<(const Term &) const;
  bool operator==(const Term &) const;
};

class TermCollection {
 private:
  std::list<Term> collection;
  
 public:
  TermCollection(const std::string &filename);
  TermCollection();

  unsigned int size();
  std::list<Term>::iterator begin();
  std::list<Term>::iterator end();
};

#endif // TERM_H
