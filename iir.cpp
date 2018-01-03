#include <cstdio>
#include <vector>
#include <string>
#include <fstream>

#include <getopt.h>

#include "indri/QueryEnvironment.hpp"

struct options {
  char *query;
  char *corpus;
  char *index;
};

options parse_command_line(int argc, char **argv) {
  int o;
  int option_index = 0;
  static struct option long_options[] = {
    {"query", required_argument, NULL, 'q'},
    {"corpus", required_argument, NULL, 'c'},
    {"index", required_argument, NULL, 'i'},
    {NULL, 0, NULL, 0}
  };
  struct options opts;

  while (true) {
    o = getopt_long(argc, argv, NULL, long_options, &option_index);
    if (o < 0) {
      break;
    }
    switch (o) {
    case 'q':
      opts.query = optarg;
      break;
    case 'c':
      opts.corpus = optarg;
      break;
    case 'i':
      opts.index = optarg;
      break;
    }
  }

  return opts;
}

std::string get_file_contents(const char *filename) {
  std::string contents;
  std::ifstream in(filename, std::ios::in | std::ios::binary);

  if (!in) {
    throw(errno);
  }
  
  in.seekg(0, std::ios::end);
  contents.resize(in.tellg());
  in.seekg(0, std::ios::beg);
  in.read(&contents[0], contents.size());

  in.close();
  
  return contents;
}

int main(int argc, char *argv[]) {
  struct options opts = parse_command_line(argc, argv);
  std::string query = get_file_contents(opts.query);

  indri::api::QueryEnvironment env;

  env.addIndex(opts.index);
  std::vector<indri::api::ScoredExtentResult> results =
    env.runQuery(query, 1000);
  std::vector<indri::api::ParsedDocument *> docs = env.documents(results);

  for (int i = 0; i < results.size(); i++) {
    int docId = results[i].document;
    indri::api::ParsedDocument *parsedDoc = docs[i];

    indri::utility::greedy_vector<indri::parse::MetadataPair>::iterator itr =
      std::find_if(parsedDoc->metadata.begin(),
                   parsedDoc->metadata.end(),
                   indri::parse::MetadataPair::key_equal("docno"));
    if (itr == parsedDoc->metadata.end()) {
      throw(std::out_of_range("docno"));
    }
    std::string docno = (char *)itr->value;

    std::cout << "0" <<
      " " << "Q0" <<
      " " << docno <<
      " " << i + 1 <<
      " " << results[i].score <<
      " " << "indri" <<
      std::endl;
        
    //    printf("0 Q0 %d %d %f indri\n", docno, i, results[i].score);
  }

  return 0;
}
