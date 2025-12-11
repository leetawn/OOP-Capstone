#include <cstdlib>
#include <iostream>
#include <stdexcept>
#include <string>
#include "binarytree.hpp"
using namespace std;

class MyBinaryTree : public BinaryTree {
    node* root;
    int size;

    // TODO
    node* create_node(int e, node* parent) {
        return nullptr;
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

   node *sibling(node *a)
    {
        if (!a || !a->parent) return NULL;
        if (a->parent->left == a) return a->parent->right;
        return a->parent->left;
    }


    int remove(node *n)
    {
        if (size <= 0)
            return -1;
        int nc = (n->left != nullptr) + (n->right != nullptr);
        if (nc >= 2)
            throw logic_error("Cannot remove " + to_string(n->elem) + " for it has 2 children");
        
        node **slot = nullptr; 
        if (n->parent)
        {
            if (n->parent->left == n)
                slot = &n->parent->left;
            if (n->parent->right == n)
                slot = &n->parent->right;
        } 
        else 
        {
            slot = &root;
        }
        
        if (slot)
        {
            *slot = nullptr;
            if (n->left)
            {
                *slot = n->left;
            }
            if (n->right)
            {
                *slot = n->right;
            }
            if (*slot)
                (*slot)->parent = n->parent;
        }
        return n->elem + (free(n), size--, 0);
    }

    
};