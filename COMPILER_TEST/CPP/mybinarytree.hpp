#include <cstdlib>
#include <iostream>
#include <stdexcept>
#include <string>
#include "binarytree.hpp"
using namespace std;

class MyBinaryTree : public BinaryTree {
    node* root;
    int size;

    node* create_node(int e, node* parent) {
        node *nn = new node{e, nullptr, nullptr, parent};
        size++;
        return nn;
    }

public:
    MyBinaryTree() {
        root = nullptr;
        size = 0;
    }
    node* addRoot(int e) {
        if (getRoot()) throw logic_error("Already has root");
        return root = create_node(e, nullptr);
    }

    node* addLeft(node* p, int e) {
        // TODO this method
        if (p->left) throw logic_error(to_string(p->elem) + " already has left child");
        return p->left = create_node(e, p);
    }

    node* addRight(node* p, int e) {
        // TODO this method
        if (p->right) throw logic_error(to_string(p->elem) + " already has right child");
        return p->right = create_node(e, p);
    }

    int getSize() {
        return size;
    }

    node* getRoot() {
        return root;
    }
};