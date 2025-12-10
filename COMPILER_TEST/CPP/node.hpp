struct node {
    int elem;
    node* left;
    node* right;
    node* parent;
    
    int depth()
    {
        if (!parent) return 0;
        return parent->depth() + 1;
    }

    int height()
    {
        if (!left && !right) return 0;
        int l = 0;
        int r = 0;
        left && (l = left->height() + 1);
        right && (r = right->height() + 1);
        if (l > r) return l;
        return r;
    }
};