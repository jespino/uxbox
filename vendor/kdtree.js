/**
 * kdtree
 *
 * Is a modified and google closure adapted kdtree implementation
 * of https://github.com/ubilabs/kd-tree-javascript.
 *
 * @author Andrey Antukh <niwi@niwi.nz>, 2016
 * @author Mircea Pricop <pricop@ubilabs.net>, 2012
 * @author Martin Kleppe <kleppe@ubilabs.net>, 2012
 * @author Ubilabs http://ubilabs.net, 2012
 * @license MIT License <http://www.opensource.org/licenses/mit-license.php>
 */

goog.provide("kdtree");
goog.provide("kdtree.KDTree");

goog.require('goog.array');
goog.require('goog.structs.Heap');
goog.require('goog.asserts');

goog.scope(function() {
  "use strict";

  const assert = goog.asserts.assert;
  const assertNumber = goog.asserts.assertNumber;
  const every = goog.array.every;

  class Node {
    constructor(obj, dimension, parent) {
      this.obj = obj;
      this.left = null;
      this.right = null;
      this.parent = parent;
      this.dimension = dimension;
    }
  }

  function buildTree(points, depth, parent, dimensions) {
    const dim = depth % dimensions;

    if (points.length === 0) {
      return null;
    }

    if (points.length === 1) {
      return new Node(points[0], dim, parent);
    }

    points.sort((a, b) => {
      return a[dim] - b[dim];
    });

    const median = Math.floor(points.length / 2);
    const node = new Node(points[median], dim, parent);
    node.left = buildTree(points.slice(0, median), depth + 1, node, dimensions);
    node.right = buildTree(points.slice(median + 1), depth + 1, node, dimensions);

    return node;
  }

  function findMin(node, dim) {
    let dimension, own, left, right, min;

    if (node === null) {
      return null;
    }

    if (node.dimension === dim) {
      if (node.left !== null) {
        return findMin(node.left, dim);
      }
      return node;
    }

    own = node.obj[dim];
    left = findMin(node.left, dim);
    right = findMin(node.right, dim);
    min = node;

    if (left !== null && left.obj[dim] < own) {
      min = left;
    }
    if (right !== null && right.obj[dim] < min.obj[dim]) {
      min = right;
    }
    return min;
  }

  function innerSearch(point, node, parent) {
    if (node === null) {
      return parent;
    }

    if (point[dim] < node.obj[dim]) {
      return innerSearch(point, node.left, node);
    } else {
      return innerSearch(point, node.right, node);
    }
  }

  function nodeSearch(point, node) {
    if (node === null) {
      return null;
    }

    if (node.obj === point) {
      return node;
    }

    if (point[node.dimension] < node.obj[node.dimension]) {
      return nodeSearch(point, node.left);
    } else {
      return nodeSearch(point, node.right);
    }
  }

  class KDTree {
    constructor(points, metric, dimensions) {
      assert(points.length !== 0);
      assertNumber(dimensions);

      this.root = buildTree(points, 0, null, dimensions);
      this.metric = metric;
      this.dimensions = dimensions;
    }

    insert(point) {
      const insertPosition = innerSearch(point, this.root, null);

      if (insertPosition === null) {
        this.root = new Node(point, 0, null);
        return;
      }

      const newNode = new Node(point,
                               (insertPosition.dimension + 1) % this.dimensions,
                               insertPosition);

      const dimension = insertPosition.dimension;
      if (point[dimension] < insertPosition.obj[dimension]) {
        insertPosition.left = newNode;
      } else {
        insertPosition.right = newNode;
      }
    }

    remove(point) {
      const node = nodeSearch(point, this.root);
      if (node === null) {
        return;
      }

      if (node.left === null && node.right === null) {
        if (node.parent === null) {
          this.root = null;
          return;
        }

        const pdim = node.parent.dimension;

        if (node.obj[pdim] < node.parent.obj[pdim]) {
          node.parent.left = null;
        } else {
          node.parent.right = null;
        }
        return;
      }

      // If the right subtree is not empty, swap with the minimum element on the
      // node's dimension. If it is empty, we swap the left and right subtrees and
      // do the same.
      let nextNode, nextObj;

      if (node.right !== null) {
        nextNode = findMin(node.right, node.dimension);
        nextObj = nextNode.obj;
        removeNode(nextNode);
        node.obj = nextObj;
      } else {
        nextNode = findMin(node.left, node.dimension);
        nextObj = nextNode.obj;
        removeNode(nextNode);
        node.right = node.left;
        node.left = null;
        node.obj = nextObj;
      }

    }

    nearest(point, maxNodes) {
      if (maxNodes === undefined) {
        maxNodes = 1;
      }

      let best = new goog.structs.Heap();

      const nearestSearch = (node) => {
        let bestChild;
        const distance = this.metric(point, node.obj);
        const dimension = node.dimension;

        if (best.getCount() < maxNodes ||  distance < best.peek()[1]) {
          best.insert(-distance, [node.obj, distance]);
          if (best.getCount() > maxNodes) {
            best.remove();
          }
        }

        if (best.isEmpty()) {
          best.insert(-distance, [node.obj, distance]);
        } else {
          if (distance < best.peek()[1]) {
            best.insert(-distance, [node.obj, distance]);

            if (best.getCount() > maxNodes) {
              best.remove();
            }
          }
        }

        if (node.right === null && node.left === null) {
          return;
        }

        if (node.right === null) {
          bestChild = node.left;
        } else if (node.left === null) {
          bestChild = node.right;
        } else {
          if (point[dimension] < node.obj[dimension]) {
              bestChild = node.left;
          } else {
              bestChild = node.right;
          }
        }

        nearestSearch(bestChild);
      }

      if(this.root) {
        nearestSearch(this.root);
      }

      result = best.getValues();
      result.sort((x) => x[1]);
      return result;
    }

    balanceFactor() {
      function height(node) {
        if (node === null) {
          return 0;
        }
        return Math.max(height(node.left), height(node.right)) + 1;
      }

      function count(node) {
        if (node === null) {
          return 0;
        }
        return count(node.left) + count(node.right) + 1;
      }

      return height(this.root) / (Math.log(count(this.root)) / Math.log(2));
    }
  }

  function distance2d(a, b){
    return Math.sqrt(Math.pow(a[0] - b[0], 2) +  Math.pow(a[1] - b[1], 2));
  }

  function create2d(points) {
    return new KDTree(points, distance2d, 2);
  };

  // Types
  kdtree.KDTree = KDTree;

  // Factory functions
  kdtree.create2d = create2d;
});
